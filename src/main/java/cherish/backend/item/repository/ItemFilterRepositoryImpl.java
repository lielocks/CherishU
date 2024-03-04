package cherish.backend.item.repository;

import cherish.backend.common.config.QueryDslConfig;
import cherish.backend.item.constant.ItemSortConstants;
import cherish.backend.item.dto.*;
import cherish.backend.member.model.Member;
import cherish.backend.member.model.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;

import static cherish.backend.category.model.QCategory.category;
import static cherish.backend.category.model.QFilter.filter;
import static cherish.backend.item.model.QItem.item;
import static cherish.backend.item.model.QItemCategory.itemCategory;
import static cherish.backend.item.model.QItemFilter.itemFilter;
import static cherish.backend.item.model.QItemJob.itemJob;
import static cherish.backend.item.model.QItemLike.itemLike;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class ItemFilterRepositoryImpl implements ItemFilterRepositoryCustom{

    private final QueryDslConfig queryDslConfig;

    @Override
    public List<ItemFilterQueryDto> findItemFilterByNameAndId(ItemFilterCondition filterCondition) {
        return queryDslConfig.jpaQueryFactory()
                .select(new QItemFilterQueryDto(
                        item.id.as("itemId"),
                        filter.id.as("filterId"),
                        itemFilter.id.as("itemFilterId"),
                        item.name.as("itemName"),
                        filter.name.as("filterName"),
                        itemFilter.name.as("itemFilterName"),
                        item.price.as("itemPrice")
                ))
                .from(itemFilter)
                .join(itemFilter.filter, filter)
                .where(
                        itemFilterNameEq(filterCondition.getItemFilterName()),
                        filterIdEq(filterCondition.getFilterId()))
                .fetch();
    }

    @Override
    public List<AgeFilterQueryDto> findItemFilterByAge(AgeFilterCondition ageCondition) {
        return queryDslConfig.jpaQueryFactory()
                .select(new QAgeFilterQueryDto(
                        item.id.as("itemId"),
                        filter.id.as("filterId"),
                        itemFilter.id.as("itemFilterId"),
                        item.name.as("itemName"),
                        filter.name.as("filterName"),
                        itemFilter.name.as("itemFilterName"),
                        item.minAge.as("minAge"),
                        item.maxAge.as("maxAge")
                ))
                .from(itemFilter)
                .join(itemFilter.filter, filter)
                .where(
                        itemFilterNameEq(ageCondition.getItemFilterName()),
                        filterIdEq(ageCondition.getFilterId()),
                        ageGoe(ageCondition.getAgeGoe()),
                        ageLoe(ageCondition.getAgeLoe()))
                .fetch();
    }

    @Override
    public Page<ItemSearchResponseDto> searchItem(ItemSearchCondition searchCondition, Member member, Pageable pageable) {
        BooleanExpression isLiked = member != null ? new CaseBuilder().when(itemLike.member.eq(member)).then(true).otherwise(false) : Expressions.asBoolean(false);

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        JPAQuery<ItemSearchResponseDto> mainSearchQuery = queryDslConfig.jpaQueryFactory()
                .selectDistinct(Projections.constructor(ItemSearchResponseDto.class,
                        item.id, item.name, item.brand, item.description, item.price, item.imgUrl, isLiked.as("isLiked"), item.views, item.modifiedDate))
                .from(item)
                .leftJoin(itemLike).on(item.id.eq(itemLike.item.id).and(member != null ? itemLike.member.id.eq(member.getId()) : null))
                .leftJoin(itemLike.member, QMember.member)
                .where(booleanBuilder);


        JPAQuery<Long> total = queryDslConfig.jpaQueryFactory()
                .select(item.id.countDistinct())
                .from(item)
                .leftJoin(itemLike).on(item.id.eq(itemLike.item.id).and(member != null ? itemLike.member.id.eq(member.getId()) : null))
                .leftJoin(itemLike.member, QMember.member)
                .where(booleanBuilder);

        if (searchCondition.getCategoryName() != null && !searchCondition.getCategoryName().isEmpty()) {
            mainSearchQuery = mainSearchQuery
                    .leftJoin(item.itemCategories, itemCategory)
                    .leftJoin(itemCategory.category, category)
                    .where(category.name.in(searchCondition.getCategoryName()));
        }

        if (isNotEmpty(searchCondition.getJobName())) {
            mainSearchQuery = mainSearchQuery
                    .leftJoin(item.itemJobs, itemJob)
                    .where(itemJob.name.in(searchCondition.getJobName()));
        }

        if (isNotEmpty(searchCondition.getSituationName()) || isNotEmpty(searchCondition.getGender()) || isNotEmpty(searchCondition.getEmotionName())) {
            List<String> conditions = new ArrayList<>();

            if (isNotEmpty(searchCondition.getSituationName())) {
                conditions.add(searchCondition.getSituationName());
            }

            if (isNotEmpty(searchCondition.getGender())) {
                conditions.add(searchCondition.getGender());
            }

            if (isNotEmpty(searchCondition.getEmotionName())) {
                conditions.add(searchCondition.getEmotionName());
            }

            List<Long> list = queryDslConfig.jpaQueryFactory()
                    .select(itemFilter.item.id)
                    .from(itemFilter)
                    .where(itemFilter.name.in(conditions))
                    .groupBy(itemFilter.item.id)
                    .having(itemFilter.name.countDistinct().goe(conditions.size()))
                    .fetch();

            mainSearchQuery.where(item.id.in(list));
            total.where(item.id.in(list));
        }

        if (isNotEmpty(searchCondition.getKeyword())) {
            String keyword = searchCondition.getKeyword();
            booleanBuilder.or(
                    item.name.contains(keyword)
                            .or(item.brand.contains(keyword))
            );

            // 키워드가 category에 속한 경우 조인 수행
            if (itemCategory.category.name.contains(keyword) != null || category.name.contains(keyword) != null) {
                mainSearchQuery = mainSearchQuery
                        .leftJoin(item.itemCategories, itemCategory)
                        .leftJoin(itemCategory.category, category);
                booleanBuilder.or(itemCategory.category.name.contains(keyword))
                        .or(category.name.contains(keyword));
            }

            // 키워드가 itemJob에 속한 경우 조인 수행
            if (itemJob.name.contains(keyword) != null) {
                mainSearchQuery = mainSearchQuery
                        .leftJoin(item.itemJobs, itemJob);
                booleanBuilder.or(itemJob.name.contains(keyword));
            }

            // 키워드가 itemFilter에 속한 경우 조인 수행
            if (itemFilter.name.contains(keyword)!= null) {
                mainSearchQuery = mainSearchQuery
                        .leftJoin(item.itemFilters, itemFilter)
                        .leftJoin(itemFilter.filter, filter);
                booleanBuilder.or(itemFilter.name.contains(keyword));
            }
        }

        List<ItemSearchResponseDto> content = mainSearchQuery
                .orderBy(getOrderSpecifier(searchCondition.getSort())) // 기본 정렬
                .where(booleanBuilder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, total::fetchFirst);
    }

    private OrderSpecifier<?> getOrderSpecifier(final String sort) {
        if (StringUtils.isEmpty(sort)) {
            return new OrderSpecifier<>(Order.ASC, item.id);
        }
        return switch (sort) {
            case ItemSortConstants.MOST_RECOMMENDED -> new OrderSpecifier<>(Order.ASC, item.id);
            case ItemSortConstants.MOST_POPULAR -> new OrderSpecifier<>(Order.DESC, item.views);
            case ItemSortConstants.LATEST -> new OrderSpecifier<>(Order.DESC, item.modifiedDate);
            case ItemSortConstants.MOST_EXPENSIVE -> new OrderSpecifier<>(Order.DESC, item.price);
            case ItemSortConstants.LEAST_EXPENSIVE -> new OrderSpecifier<>(Order.ASC, item.price);
            default -> throw new IllegalArgumentException("Sort error");
        };
    }

    private BooleanExpression itemFilterNameEq(String itemFilterName) {
        return hasText(itemFilterName) ? itemFilter.name.containsIgnoreCase(itemFilterName) : null;
    }

    private BooleanExpression filterIdEq(Long filterId) {
        return hasText(String.valueOf(filterId)) ? filter.id.eq(filterId) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? item.maxAge.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? item.minAge.loe(ageLoe) : null;
    }

}

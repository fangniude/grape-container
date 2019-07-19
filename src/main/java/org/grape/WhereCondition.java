package org.grape;

import io.ebean.Expression;
import io.ebean.ExpressionFactory;
import io.ebean.Junction;
import io.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

public interface WhereCondition extends Serializable {

    <T extends BaseDomain> Expression whereExpression(Query<T> query);

    enum BoolOperator implements Serializable {
        /**
         *
         */
        AND,
        /**
         *
         */
        OR
    }

    enum ExpressOperator implements Serializable {

        /**
         * 等于
         */
        EQ("="),
        /**
         * 不等于
         */
        UEQ("!="),
        /**
         * 大于
         */
        GT(">"),
        /**
         * 小于
         */
        LT("<"),
        /**
         * 大于等于
         */
        GET(">="),
        /**
         * 小于等于
         */
        LET("<="),
        /**
         * 全模糊匹配
         */
        LIKE("like"),
        /**
         * 左侧模糊匹配
         */
        LLIKE("like"),
        /**
         * 右侧模糊匹配
         */
        RLIKE("like"),
        /**
         * between and
         */
        BETWEEN("between"),
        /**
         * in
         */
        IN("in"),
        /**
         * not in
         */
        NOTIN("not in");

        @Getter
        private final String val;

        ExpressOperator(String val) {
            this.val = val;
        }
    }

    @NoArgsConstructor
    class BaseBoolCondition implements WhereCondition, Serializable {
        private BoolOperator operator;

        private List<? extends WhereCondition> conditions;

        public BaseBoolCondition(BoolOperator operator, @NonNull List<? extends WhereCondition> conditions) {
            this.operator = operator;
            this.conditions = conditions;
            if (conditions.size() <= 1) {
                throw new RuntimeException("conditions size must >= 2");
            }
        }

        @Override
        public <T extends BaseDomain> Expression whereExpression(Query<T> query) {
            ExpressionFactory factory = query.getExpressionFactory();
            Junction<T> junction = this.operator == BoolOperator.AND ? factory.conjunction(query) : factory.disjunction(query);
            for (WhereCondition condition : conditions) {
                junction.add(condition.whereExpression(query));
            }
            return junction;
        }
    }

    @NoArgsConstructor
    class AndCondition extends BaseBoolCondition implements Serializable {
        public AndCondition(List<? extends WhereCondition> conditions) {
            super(BoolOperator.AND, conditions);
        }
    }

    @NoArgsConstructor
    class OrCondition extends BaseBoolCondition implements Serializable {
        public OrCondition(List<? extends WhereCondition> conditions) {
            super(BoolOperator.OR, conditions);
        }
    }

    @ApiModel("【通用】过滤条件")
    @Getter
    @Setter
    @NoArgsConstructor
    class ExpressCondition implements WhereCondition, Serializable {
        @ApiModelProperty(value = "列名", required = true)
        private String column;
        @ApiModelProperty(value = "操作: EQ(=), UEQ(!=), GT(>), LT(<), GET(>=), LET(<=), LIKE(%text%), LLIKE(text%), RLIKE(%text), BETWEEN(between x and y), IN([1,2,3]), NOTIN([1,2,3])", required = true)
        private ExpressOperator operator;
        @ApiModelProperty(value = "值列表，一般为一个，between为2个，in可以为多个", required = true)
        private String[] values;

        public ExpressCondition(@NonNull String column, @NonNull ExpressOperator operator, @NonNull String... values) {
            this.column = column;
            this.operator = operator;
            this.values = values;
        }

        @NonNull
        private String inValues() {
            StringBuilder sb = new StringBuilder(values[0]);
            for (int i = 1; i < values.length; i++) {
                sb.append(",").append(values[i]);
            }
            return sb.toString();
        }

        @Override
        public <T extends BaseDomain> Expression whereExpression(Query<T> query) {
            ExpressionFactory factory = query.getExpressionFactory();
            switch (operator) {
                case EQ:
                    return factory.ieq(column, values[0]);
                case UEQ:
                    return factory.ne(column, values[0]);
                case GT:
                    return factory.gt(column, values[0]);
                case LT:
                    return factory.lt(column, values[0]);
                case GET:
                    return factory.ge(column, values[0]);
                case LET:
                    return factory.le(column, values[0]);
                case LIKE:
                    return factory.icontains(column, values[0]);
                case LLIKE:
                    return factory.istartsWith(column, values[0]);
                case RLIKE:
                    return factory.iendsWith(column, values[0]);
                case BETWEEN:
                    return factory.between(column, values[0], values[1]);
                case IN:
                    return factory.in(column, values);
                case NOTIN:
                    return factory.notIn(column, values);
                default:
                    throw new RuntimeException(String.format("unsupported operator: %s", operator.name()));

            }
        }
    }

    @NoArgsConstructor
    class StringCondition implements WhereCondition, Serializable {
        private String whereSql;

        public StringCondition(String whereSql) {
            this.whereSql = whereSql;
        }

        @Override
        public <T extends BaseDomain> Expression whereExpression(Query<T> query) {
            return query.getExpressionFactory().raw(whereSql);
        }
    }
}

package io.nbrs.dynamicql.model;

/**
 * Created by Antonio Zaitoun on 2019-12-27.
 */
public class QueryParameter {


    /**
     * The name of the parameter.
     */
    private String name;

    /**
     * The parameter type. valid values are:
     * `string`, `number`, `boolean`, `date`.
     */
    private String type;

    /**
     * Value could be of type:
     * java.lang.String,
     * java.lang.Number,
     * java.lang.Boolean,
     * java.util.Date
     */
    private Object defaultValue;

    /**
     * Override means that the value is automatically overridden by a resolver.
     */
    private boolean override;

    /**
     * If the parameter is required by user to input. This means that the query cannot use a default value here.
     */
    private boolean required;

    /**
     * If the value can be null.
     */
    private boolean nullable;

    /**
     * The name of the resolver function to get the value if needed.
     */
    private String resolver;

    /**
     * The index of the parameter in the query.
     * For example, in a query `select * from users where id = ?`
     * The position of the parameter will be 0.
     * <p>
     * Position is an array because the parameter may be used multiple times.
     */
    private int[] positions;

    public QueryParameter(String name, int[] positions) {
        this.name = name;
        this.positions = positions;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isOverride() {
        return override;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getResolver() {
        return resolver;
    }

    public int[] getPositions() {
        return positions;
    }
}

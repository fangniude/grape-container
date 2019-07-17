package org.grape;

import io.ebean.annotation.Cache;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * @author lewis
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@Cache(naturalKey = "id")
@MappedSuperclass
public abstract class BaseDomain extends BaseModel {

    /**
     * 用逻辑ID，组合主键用id静态方法拼接
     */
    @Id
    protected String id;

    /**
     * 通用名称
     */
    @Column(nullable = false)
    protected String name;

    /**
     * 通用备注
     */
    protected String remark;

    /**
     * 插入时间
     */
    @CreatedTimestamp
    protected LocalDateTime whenCreated;

    /**
     * 最后更新时间
     */
    @UpdatedTimestamp
    protected LocalDateTime whenUpdated;

    public static String id(String... strings) {
        return String.join("__", strings);
    }

    @Override
    protected String getDbName() {
        Class<? extends BaseDomain> domainClass = getClass();
        return getDbNameByDomain(domainClass);
    }

    private static String getDbNameByDomain(Class<? extends BaseDomain> domainClass) {
        String pluginName = domainClass.getPackage().getName().split("\\.")[0];
        return GrapeApplication.getPlugin(pluginName).getDbName();
    }

    @Override
    protected void save() {
        super.save();
    }

    @Override
    protected void update() {
        super.update();
    }

    @Override
    protected void insert() {
        super.insert();
    }

    public static class Finder<T extends BaseDomain> extends io.ebean.Finder<String, T> {
        protected final Class<T> type;

        public Finder(Class<T> type) {
            super(type, getDbNameByDomain(type));
            this.type = type;
        }
    }
}

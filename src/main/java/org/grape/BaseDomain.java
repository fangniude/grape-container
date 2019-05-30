package org.grape;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author lewis
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@Cache(naturalKey = "primary_key")
@MappedSuperclass
public abstract class BaseDomain extends Model {

    /**
     * 用逻辑ID，组合主键用id静态方法拼接
     */
    @Id
    protected String id;

    /**
     * 通用名称
     */
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
    public void save() {
        super.save();
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void insert() {
        super.insert();
    }

    public static class Finder<T> extends io.ebean.Finder<Long, T> {
        protected final Class<T> type;

        public Finder(Class<T> type) {
            super(type);
            this.type = type;
        }

        public Optional<T> byIdo(Long id) {
            return Optional.ofNullable(byId(id));
        }

        public Optional<T> byKey(String key) {
            return db().find(type).where().eq("primary_key", key).findOneOrEmpty();
        }
    }
}

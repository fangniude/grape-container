package org.grape;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author lewis
 */
@Getter
@Setter
@NoArgsConstructor
@Cache(naturalKey = "primary_key")
@MappedSuperclass
public abstract class BaseGrapeModel extends Model {
    @Id
    protected String id;

    protected String name;

    protected String remark;

//    protected Boolean auth; todo may be used in data permission

    @Version
    protected Long version;

    @CreatedTimestamp
    protected LocalDateTime whenCreated;

    @UpdatedTimestamp
    protected LocalDateTime whenUpdated;

    public static String id(String... strings) {
        return String.join("__", strings);
    }

    @NonNull
    public abstract String id();

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

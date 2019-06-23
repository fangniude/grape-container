package org.grape;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.bean.EntityBean;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseModel {

    /**
     * db name
     *
     * @return db name
     */
    protected abstract String getDbName();

    protected EbeanServer ebeanServer() {
        return Ebean.getServer(getDbName());
    }

    /**
     * Return the underlying 'default' Database.
     * <p>
     * This provides full access to the API such as explicit transaction demarcation etc.
     * <p>
     * Example:
     * <pre>{@code
     *
     * try (Transaction transaction = Customer.db().beginTransaction()) {
     *
     *   // turn off cascade persist for this transaction
     *   transaction.setPersistCascade(false);
     *
     *   // extra control over jdbc batching for this transaction
     *   transaction.setBatchGetGeneratedKeys(false);
     *   transaction.setBatchMode(true);
     *   transaction.setBatchSize(20);
     *
     *   Customer customer = new Customer();
     *   customer.setName(&quot;Roberto&quot;);
     *   customer.save();
     *
     *   Customer otherCustomer = new Customer();
     *   otherCustomer.setName("Franko");
     *   otherCustomer.save();
     *
     *   transaction.commit();
     *
     * }
     *
     * }</pre>
     */
    protected Database db() {
        return DB.byName(getDbName());
    }

    /**
     * Marks the entity bean as dirty.
     * <p>
     * This is used so that when a bean that is otherwise unmodified is updated the version
     * property is updated.
     * <p>
     * An unmodified bean that is saved or updated is normally skipped and this marks the bean as
     * dirty so that it is not skipped.
     * <p>
     * <pre>{@code
     *
     * Customer customer = Customer.find.byId(id);
     *
     * // mark the bean as dirty so that a save() or update() will
     * // increment the version property
     * customer.markAsDirty();
     * customer.save();
     *
     * }</pre>
     *
     * @see Database#markAsDirty(Object)
     */
    protected void markAsDirty() {
        db().markAsDirty(this);
    }

    /**
     * Mark the property as unset or 'not loaded'.
     * <p>
     * This would be used to specify a property that we did not wish to include in a stateless update.
     * </p>
     * <pre>{@code
     *
     *   // populate an entity bean from JSON or whatever
     *   User user = ...;
     *
     *   // mark the email property as 'unset' so that it is not
     *   // included in a 'stateless update'
     *   user.markPropertyUnset("email");
     *
     *   user.update();
     *
     * }</pre>
     *
     * @param propertyName the name of the property on the bean to be marked as 'unset'
     */
    protected void markPropertyUnset(String propertyName) {
        ((EntityBean) this)._ebean_getIntercept().setPropertyLoaded(propertyName, false);
    }

    /**
     * Insert or update this entity depending on its state.
     * <p>
     * Ebean will detect if this is a new bean or a previously fetched bean and perform either an
     * insert or an update based on that.
     *
     * @see Database#save(Object)
     */
    protected void save() {
        db().save(this);
    }

    /**
     * Flush any batched changes to the database.
     * <p>
     * When using JDBC batch flushing occurs automatically at commit() time or when the batch size
     * is reached. This provides the ability to manually flush the batch.
     * </p>
     */
    protected void flush() {
        db().flush();
    }

    /**
     * Update this entity.
     *
     * @see Database#update(Object)
     */
    protected void update() {
        db().update(this);
    }

    /**
     * Insert this entity.
     *
     * @see Database#insert(Object)
     */
    protected void insert() {
        db().insert(this);
    }

    /**
     * Delete this bean.
     * <p>
     * This will return true if the bean was deleted successfully or JDBC batch is being used.
     * </p>
     * <p>
     * If there is no current transaction one will be created and committed for
     * you automatically.
     * </p>
     * <p>
     * If the Bean does not have a version property (or loaded version property) and
     * the bean does not exist then this returns false indicating that nothing was
     * deleted. Note that, if JDBC batch mode is used then this always returns true.
     * </p>
     *
     * @see Database#delete(Object)
     */
    protected boolean delete() {
        return db().delete(this);
    }

    /**
     * Delete a bean permanently without soft delete.
     * <p>
     * This is used when the bean contains a <code>@SoftDelete</code> property and we
     * want to perform a hard/permanent delete.
     * </p>
     *
     * @see Database#deletePermanent(Object)
     */
    protected boolean deletePermanent() {
        return db().deletePermanent(this);
    }

    /**
     * Refreshes this entity from the database.
     *
     * @see Database#refresh(Object)
     */
    protected void refresh() {
        db().refresh(this);
    }
}

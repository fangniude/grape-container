package org.grape;

import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface CrudService<T extends BaseDomain> {
    /**
     * 插入
     *
     * @param domain domain
     */
    void insert(T domain);

    /**
     * 更新(直接update)
     *
     * @param domain domain
     */
    void update(T domain);

    /**
     * 插入 或 之前读出来了，现在要保存(先读后update)
     *
     * @param domain domain
     */
    void save(T domain);

    /**
     * 根据ID查一次数据库，判断是插入还是更新
     *
     * @param domain domain
     */
    void insertOrUpdate(T domain);

    /**
     * 根据id是否为null，判断是插入还是更新
     *
     * @param domain domain
     */
    void insertOrUpdateByIdIsNull(T domain);

    /**
     * delete
     *
     * @param domain domain
     * @return success
     */
    boolean delete(T domain);

    /**
     * Delete a bean permanently without soft delete.
     * <p>
     * This is used when the bean contains a <code>@SoftDelete</code> property and we
     * want to perform a hard/permanent delete.
     * </p>
     *
     * @param domain domain
     * @return success
     */
    boolean deletePermanent(T domain);

    /**
     * delete by id
     *
     * @param id id
     */
    void deleteById(Long id);

    /**
     * all
     *
     * @return all
     */
    @NonNull
    List<T> all();

    /**
     * find by id
     *
     * @param id id
     * @return optional
     */
    Optional<T> findById(Long id);

    /**
     * find by id
     *
     * @param id id
     * @return nullable
     */
    @Nullable
    T findByIdNullable(Long id);

    /**
     * find by id
     *
     * @param id id
     * @return throw RuntimeException if null
     */
    @NonNull
    T findByIdNonNull(Long id);

    /**
     * query for tables
     *
     * @param query query condition
     * @return paged result
     */
    PagedResultList<T> find(SimpleQuery query);
}

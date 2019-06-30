package org.grape;

import io.ebean.PagedList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResultList<T> implements Serializable {
    private int pageIndex;
    private int pageSize;
    private int total;
    private int totalPage;
    private List<T> data;

    public PagedResultList(PagedList<T> pagedList) {
        pagedList.loadCount();
        this.pageIndex = pagedList.getPageIndex();
        this.pageSize = pagedList.getPageSize();
        this.total = pagedList.getTotalCount();
        this.totalPage = pagedList.getTotalPageCount();
        this.data = pagedList.getList();
    }
}

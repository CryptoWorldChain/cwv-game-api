package org.brewchain.cwv.common.util;


import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.common.service.Country.PageOut;

import lombok.Data;

/**
 * 分页处理
 * @author moon
 *
 */
@Data
public class PageUtil {
	
	private int offset = 0;//偏移量
	
	private int limit = 10;//查询数量
	
	private PageOut.Builder pageOut = PageOut.newBuilder();
	public PageUtil(String pageIndex , String pageSize) {
		super();
		if(offset<0 || limit<0){
			throw new IllegalArgumentException("分页参数错误");
		}
		this.limit = Integer.parseInt(StringUtils.isEmpty(pageSize)?"10":pageSize);
		int pageIndexInt = Integer.parseInt(StringUtils.isEmpty(pageIndex)?"1": pageIndex);
		this.offset = pageIndexInt==0? 0 : (pageIndexInt-1) * this.limit  ;
		pageOut.setPageIndex(StringUtils.isEmpty(pageIndex)?"1": pageIndex);
		pageOut.setPageSize(StringUtils.isEmpty(pageSize)?"10": pageSize);
	}
	
	public PageUtil(int pageIndex , int pageSize) {
		super();
		if(offset<0 || limit<0){
			throw new IllegalArgumentException("分页参数错误");
		}
		this.limit = pageSize;
		this.offset = pageIndex==0? 0 : (pageIndex-1) * this.limit  ;
	}
	
	public void setTotalCount(int sum){
		pageOut.setTotalCount(sum+"");
	}


}

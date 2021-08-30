package com.howfun.model;

import lombok.Data;

import java.util.List;

/**
 * Response包装类
 * @param <T> Response的类型
 */
@Data
public class ResObject<T> {

	private String resCode;
	private String resMessage;
	private Object resObject;
	private List<T> resList;

	public ResObject(String resCode,String resMessage,Object resObject,List<T> resList){
		this.resCode = resCode;
		this.resMessage = resMessage;
		this.resObject = resObject;
		this.resList = resList;
	}
}

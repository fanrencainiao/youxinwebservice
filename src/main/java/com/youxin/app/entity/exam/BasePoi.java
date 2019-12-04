package com.youxin.app.entity.exam;

import com.alibaba.fastjson.annotation.JSONField;

import io.swagger.annotations.ApiModelProperty;

public class BasePoi {
	@ApiModelProperty(value = "地址")
	private String address;
	@ApiModelProperty(value = "距离")
	private int distance; 
	@ApiModelProperty(value = "纬度")
	@JSONField(serialize = false)
	private double latitude; 
	@ApiModelProperty(value = "经度")
	@JSONField(serialize = false)
	private double longitude; 
	@ApiModelProperty(value = "页码")
	@JSONField(serialize = false)
	private int pageIndex=0;
	@ApiModelProperty(value = "长度")
	@JSONField(serialize = false)
	private int pageSize=10;
	@ApiModelProperty(value = "索引")
	@JSONField(serialize = false)
	private int poiId; 
	@ApiModelProperty(value = "标签")
	private String tags; 
	@ApiModelProperty(value = "名称")
	private String title; 

	public String getAddress() {
		return address;
	}

	public int getDistance() {
		return distance;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getPoiId() {
		return poiId;
	}

	public String getTags() {
		return tags;
	}

	public String getTitle() {
		return title;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setPoiId(int poiId) {
		this.poiId = poiId;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}

package com.example.accessingdatar2dbc.mapper;

import java.util.List;

import com.example.accessingdatar2dbc.Customer;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MybatisMapper {
	@Select("SELECT id, first_name as firstName, last_name as lastName from Customer")
	List<Customer> getList();
}

package com.mak.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Administrator on 2017/7/22 0022.
 */
public interface ContentRepository extends CrudRepository<Content, Integer> {
    List<Content> findByWechatIdAndPt(String wechatId, String pt);
    List<Content> findByWechatIdAndContentMD5(String wechatId,String contentMD5);
}

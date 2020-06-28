package com.distributionlock.distributionlock.mapper;

import com.distributionlock.distributionlock.entity.Goods;
import org.apache.ibatis.annotations.Param;

public interface GoodsMapper {

    Goods getGoodsById(@Param("goodsId")Long goodsId);

    int updateStockById(Long goodsId,Integer stock);

}

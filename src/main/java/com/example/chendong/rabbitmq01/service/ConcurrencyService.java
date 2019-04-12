package com.example.chendong.rabbitmq01.service;

import com.example.chendong.rabbitmq01.entity.Product;
import com.example.chendong.rabbitmq01.entity.ProductRobbingRecord;
import com.example.chendong.rabbitmq01.mapper.ProductMapper;
import com.example.chendong.rabbitmq01.mapper.ProductRobbingRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConcurrencyService {

    private static final Logger log = LoggerFactory.getLogger(ConcurrencyService.class);

    private static final String ProductNo="product_10010";

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductRobbingRecordMapper productRobbingRecordMapper;

    /*public void manageRobbing(String mobile){
        try{
            Product product = productMapper.selectByProductNo(ProductNo);
            if(product != null && product.getTotal()>0){
                log.info("当前手机号：{}  恭喜您抢到单了！",mobile);

                productMapper.updateTotal(product);
            }else{
                log.info("当前手机号： {} 抢不到单",mobile);
            }
        }catch (Exception e){
            log.error("处理抢单发生异常：mobile={}",mobile);
        }
    }*///v1.0
       //v2.0
    public void manageRobbing(String mobile){
        try{
            Product product = productMapper.selectByProductNo(ProductNo);
            if(product != null && product.getTotal()>0){
                int result = productMapper.updateTotal(product);
                if(result > 0){
                    ProductRobbingRecord productRobbingRecord = new ProductRobbingRecord();
                    //存入手机号
                    productRobbingRecord.setMobile(mobile);
                    //确定商品编号
                    productRobbingRecord.setProductId(product.getId());
                    //存入到抢单信息表中
                    productRobbingRecordMapper.insertSelective(productRobbingRecord);
                }
            }else{
                log.info("当前手机号： {} 抢不到单",mobile);
            }
        }catch (Exception e){
            log.error("处理抢单发生异常：mobile={}",mobile);
        }
    }
}

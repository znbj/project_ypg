package cn.itcast.core.controller.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.goods.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @RequestMapping("/search.do")
    public PageResult search(Integer page,Integer rows, @RequestBody Goods goods) {
        return goodsService.searchForManager(page, rows, goods);
    }

    @RequestMapping("/updateStatus.do")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids,status);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    @RequestMapping("/delete.do")
    public Result delete(Long[] ids,String status) {
        try {
            goodsService.delete(ids,status);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

}

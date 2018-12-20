package cn.itcast.core.controller.cart;

import cn.itcast.core.entity.Result;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/cart")
public class CartController {

    @CrossOrigin({"http://localhost:9003"})
    @RequestMapping("/addGoodsToCartList.do")
    public Result addGoodsToCartList(Long itemId, String num, HttpServletResponse response, HttpServletRequest request) {

        try {
//            // 服务器端支持CORS
//            response.setHeader("Access-Control-Allow-Origin", "http://localhost:9003");
//            // 携带cookie
//            response.setHeader("Access-Control-Allow-Credentials", "true");


            return new Result(true, "保存成功");

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "保存失败");
        }

    }
}

package cn.itcast.core.controller.spec;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.spec.SpecService;
import cn.itcast.core.vo.specificationVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class specController {
    @Reference
    SpecService specService;

    @RequestMapping("/findPage.do")
    public PageResult findPage(Integer page,Integer rows){
        return  specService.findPage(page,rows);
    }



    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification){
        return  specService.search(page,rows,specification);
    }

    @RequestMapping("/add.do")
    public Result add(@RequestBody specificationVo specificationVo) {
        try {
            specService.add(specificationVo);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }

    /**
     * 修改回显
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public specificationVo findOne(Long id) {
       return specService.findOne(id);
    }

    /**
     * 修改
     * @param specificationVo
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody specificationVo specificationVo) {
        try {
            specService.update(specificationVo);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids) {
        try {
            specService.dele(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    @RequestMapping("/selectOptionList.do")
    public List<Map<String,String>> selectOptionList() {
        return specService.selectOptionList();
    }

}

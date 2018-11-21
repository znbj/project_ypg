package cn.itcast.core.controller.template;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.tempalte.Type_TemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class Type_TemplateController {
    @Reference
    private Type_TemplateService type_templateService;




    @RequestMapping("/findOne.do")
    public TypeTemplate findOne(Long id) {
        return type_templateService.findOne(id);
    }

    @RequestMapping("/findBySpecList.do")
    public List<Map> findBySpecList(long id) {
    return type_templateService.findBySpecList(id);
    }
}

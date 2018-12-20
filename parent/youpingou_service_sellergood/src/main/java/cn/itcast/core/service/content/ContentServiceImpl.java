package cn.itcast.core.service.content;

import java.util.List;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.ad.ContentQuery;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import org.springframework.data.redis.core.RedisTemplate;
import javax.annotation.Resource;


@Service
public class ContentServiceImpl implements ContentService {

	@Resource
	private ContentDao contentDao;

	@Resource
	private RedisTemplate redisTemplate;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Content content) {
		clearCache(content.getCategoryId());
		contentDao.insertSelective(content);
	}

	@Override
	public void edit(Content content) {
		Long newCategoryId = content.getCategoryId();
		Long oldCategoryId = contentDao.selectByPrimaryKey(content.getId()).getCategoryId();
		if (oldCategoryId != newCategoryId) {
			clearCache(oldCategoryId);
			clearCache(newCategoryId);
		} else {
			clearCache(newCategoryId);
		}
		contentDao.updateByPrimaryKeySelective(content);
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				clearCache(contentDao.selectByPrimaryKey(id).getCategoryId());
				contentDao.deleteByPrimaryKey(id);
			}
		}
	}

	/**
	 * 从redis缓存拿数据
	 * @param categoryId
	 * @return
	 */
	@Override
	public List<Content> findByCategoryId(Long categoryId) {
		List<Content> list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
		if (list == null) {
			synchronized (this) {
				list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
				if (list == null) {
					ContentQuery contentQuery = new ContentQuery();
					contentQuery.createCriteria().andCategoryIdEqualTo(categoryId);
					list = contentDao.selectByExample(contentQuery);
					redisTemplate.boundHashOps("content").put(categoryId, list);
				}
			}
		}
		return list;
	}

	private void clearCache(Long categoryId) {
		redisTemplate.boundHashOps("content").delete(categoryId);
	}
}

package com.xm.module.dict.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcDept;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.TcDeptVo;
import com.xm.auth.service.TcDeptService;
import com.xm.auth.service.TcUserService;
import com.xm.core.cache.config.CustomCacheConfig;
import com.xm.module.core.params.QueryData;
import com.xm.module.dict.domain.dto.SaveOrUpdateDictDto;
import com.xm.module.dict.domain.entity.TcDict;
import com.xm.module.dict.domain.entity.TcDictGroup;
import com.xm.module.dict.domain.query.DictQuery;
import com.xm.module.dict.domain.vo.DictTree;
import com.xm.module.dict.enums.DictQueryTypeEnum;
import com.xm.module.dict.mapper.TcDictMapper;
import com.xm.module.dict.service.TcDictService;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class TcDictServiceImpl implements TcDictService {

    private final TcDictMapper dictMapper;

    private final TcDeptService deptService;

    //redis key
    private final String selectByPage="dict_selectByPage#u";
    private final String getDictByGroupKey="dict_getDictByGroupKey#u";

    private final String getReverseDictByGroupKey="dict_getReverseDictByGroupKey#u";
    private final String getDictListByGroupKey="dict_getDictListByGroupKey#u";

    private final String getDictMappingByQuery="dict_getDictMappingByQuery#u";

    private final String getReverseDictStrMappingByQuery="dict_getReverseDictStrMappingByQuery#u";

    private final String getDictStrMappingByQuery="dict_getDictStrMappingByQuery#u";

    @Override
    @Cacheable(value = selectByPage,keyGenerator = CustomCacheConfig.keyGeneratorName)
    public Page<TcDict> selectByPage(QueryData<TcDict> queryData) {
        QueryWrapper<TcDict> queryWrapper = queryData.generateQueryWrapper();
        queryWrapper.eq("judge_enable",1);
        queryWrapper.orderByDesc("create_date");
        Page<TcDict> page=new Page<>(queryData.getCurrent(), queryData.getSize());
        return dictMapper.selectPage(page,queryWrapper);
    }


    @Override
    @Cacheable(value = getDictByGroupKey,keyGenerator = CustomCacheConfig.keyGeneratorName)
    public Map<String, String> getDictByGroupKey(String groupKey) {
        TcDictService beanByClass = SpringBeanUtil.getBeanByClass(TcDictService.class);
        List<TcDict> dictList=beanByClass.getDictListByGroupKey(groupKey);
        Map<String, String> map=new HashMap<>();
        if (CollectionUtil.isNotEmpty(dictList)){
            dictList.forEach(tcDict -> {
                map.put(tcDict.getDictCode(),tcDict.getDictLabel());
            });
        }
        return map;
    }

    @Override
    @Cacheable(value = getReverseDictByGroupKey,keyGenerator = CustomCacheConfig.keyGeneratorName)
    public Map<String, String> getReverseDictByGroupKey(String groupKey) {
        TcDictService beanByClass = SpringBeanUtil.getBeanByClass(TcDictService.class);
        List<TcDict> dictList=beanByClass.getDictListByGroupKey(groupKey);
        Map<String, String> map=new HashMap<>();
        if (CollectionUtil.isNotEmpty(dictList)){
            dictList.forEach(tcDict -> {
                map.put(tcDict.getDictLabel(),tcDict.getDictCode());
            });
        }
        return map;
    }

    @Override
    @Cacheable(value = getDictListByGroupKey,keyGenerator = CustomCacheConfig.keyGeneratorName)
    public List<TcDict> getDictListByGroupKey(String groupKey) {
        if (StrUtil.isBlank(groupKey)){
            throw new CommonException("字典组KEY不能为空");
        }
        LambdaQueryWrapper<TcDict> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcDict::getGroupKey,groupKey).eq(TcDict::getJudgeEnable,1);

        List<TcDict> dictList=dictMapper.selectList(lambdaQueryWrapper);
        if (dictList==null){
            return new ArrayList<>();
        }else {
            return dictList;
        }
    }



    private List<DictTree> convertDictToDictTreeByLevel(List<TcDict> dictList, int level){
        List<TcDict> topDictList = dictList.stream().filter(item -> level == item.getLevel()).collect(Collectors.toList());
        Map<String, List<TcDict>> parentIdMapping = dictList.stream().collect(Collectors.groupingBy(TcDict::getParentId));
        return convertDictToDictTreeRecursion(topDictList,parentIdMapping);
    }

    private List<DictTree> convertDictToDictTreeRecursion(List<TcDict> dictList,Map<String, List<TcDict>> parentIdMapping){
        if (CollectionUtil.isEmpty(parentIdMapping)){
            return null;
        }
        if (CollectionUtil.isEmpty(dictList)){
            return null;
        }
        List<DictTree> dictTreeList=new ArrayList<>();
        for (TcDict dict:dictList){
            DictTree tree=new DictTree();
            tree.setLabel(dict.getDictLabel());
            tree.setValue(dict.getDictCode());

            List<TcDict> childrenDictList=parentIdMapping.get(dict.getId());
            if (CollectionUtil.isEmpty(childrenDictList)){
                tree.setChildren(null);
                tree.setIsLeaf(true);
            }else {
                tree.setChildren(convertDictToDictTreeRecursion(childrenDictList,parentIdMapping));
                tree.setIsLeaf(false);
            }
            dictTreeList.add(tree);
        }
        return dictTreeList;
    }


    private List<DictTree> convertDeptToDictTreeByLevel(List<TcDeptVo> deptVoList,int level){
        List<TcDeptVo> topDeptVoList = deptVoList.stream().filter(item -> level == item.getLevel()).collect(Collectors.toList());
        Map<String, List<TcDeptVo>> parentIdMapping = deptVoList.stream().collect(Collectors.groupingBy(TcDeptVo::getParentId));
        return convertDeptToDictTreeRecursion(topDeptVoList,parentIdMapping);
    }

    private List<DictTree> convertDeptToDictTreeRecursion(List<TcDeptVo> deptVoList,Map<String, List<TcDeptVo>> parentIdMapping){
        if (CollectionUtil.isEmpty(parentIdMapping)){
            return null;
        }
        if (CollectionUtil.isEmpty(deptVoList)){
            return null;
        }
        List<DictTree> dictTreeList=new ArrayList<>();
        for (TcDeptVo tcDeptVo:deptVoList){
            DictTree tree=new DictTree();
            tree.setLabel(tcDeptVo.getName());
            tree.setValue(tcDeptVo.getId());

            List<TcDeptVo> childrenDeptVoList=parentIdMapping.get(tcDeptVo.getId());
            if (CollectionUtil.isEmpty(childrenDeptVoList)){
                tree.setChildren(null);
                tree.setIsLeaf(true);
            }else {
                tree.setChildren(convertDeptToDictTreeRecursion(childrenDeptVoList,parentIdMapping));
                tree.setIsLeaf(false);
            }
            dictTreeList.add(tree);
        }
        return dictTreeList;
    }

    @Override
    @Cacheable(value = getDictMappingByQuery,keyGenerator = CustomCacheConfig.keyGeneratorName)
    public Map<String, Object> getDictMappingByQuery(List<DictQuery> dictQueryList) {
        Map<String, Object> result=new HashMap<>();
        if (CollectionUtil.isEmpty(dictQueryList)){
            return  result;
        }
        TcDictService dictService = SpringBeanUtil.getBeanByClass(TcDictService.class);
        //获取部门人员
        List<TcDeptVo> tcDeptVos = deptService.selectAllByList(new QueryData<>());
        List<DictTree> deptDictTreeList = convertDeptToDictTreeByLevel(tcDeptVos, 1);
        if (CollectionUtil.isEmpty(deptDictTreeList)){
            deptDictTreeList=new ArrayList<>();
        }
        Map<String, String> deptDicMapping = tcDeptVos.stream().collect(Collectors.toMap(TcDept::getId, TcDept::getName));
        //获取用户
        TcUserService userService = SpringBeanUtil.getBeanByClass(TcUserService.class);
        List<TcUser> tcUsers = userService.selectByList(new QueryData<>());
        Map<String, String> userDicMapping = tcUsers.stream().collect(Collectors.toMap(TcUser::getId, TcUser::getNickName));

        for (DictQuery query:dictQueryList){
            String dictQueryType = query.getDictQueryType();
            if (StrUtil.isBlank(dictQueryType)){
                throw new CommonException("字典查询类型不能为空");
            }
            String dictMappingKey = query.getDictMappingKey();
            if (StrUtil.isBlank(dictMappingKey)){
                throw new CommonException("字典映射编码不能为空");
            }
            String dictGroupCode = query.getDictGroupCode();
            if (StrUtil.isBlank(dictGroupCode)){
                throw new CommonException("字典组编码不能为空");
            }

            if (DictQueryTypeEnum.COMMON.name().equals(dictQueryType)){
                Map<String, String> dictMapping = dictService.getDictByGroupKey(dictGroupCode);
                result.put(dictMappingKey,dictMapping);
            }

            if (DictQueryTypeEnum.TREE.name().equals(dictQueryType)){
                List<TcDict> dictList = dictService.getDictListByGroupKey(dictGroupCode);
                List<DictTree> dictTreeList = convertDictToDictTreeByLevel(dictList, 0);
                if (CollectionUtil.isEmpty(dictTreeList)){
                    dictTreeList=new ArrayList<>();
                }
                result.put(dictMappingKey,dictTreeList);
            }

            if (DictQueryTypeEnum.DEPARTMENT_COMMON.name().equals(dictQueryType)){
                result.put(dictMappingKey,deptDicMapping);
            }

            if (DictQueryTypeEnum.DEPARTMENT_TREE.name().equals(dictQueryType)){
                result.put(dictMappingKey,deptDictTreeList);
            }

            if (DictQueryTypeEnum.USER_COMMON.name().equals(dictQueryType)){
                result.put(dictMappingKey,userDicMapping);
            }
        }
        return  result;
    }

    @Override
    @Cacheable(value = getReverseDictStrMappingByQuery,keyGenerator = CustomCacheConfig.keyGeneratorName)
    public Map<String,Map<String,String>> getReverseDictStrMappingByQuery(List<DictQuery> dictQueryList) {
        Map<String,Map<String,String>> result=new HashMap<>();
        if (CollectionUtil.isEmpty(dictQueryList)){
            return  result;
        }
        TcDictService dictService = SpringBeanUtil.getBeanByClass(TcDictService.class);
//        //获取部门人员
//        List<TcDeptVo> tcDeptVos = deptService.selectAllByList(new QueryParams<>());
//        Map<String, String> reverseDeptDicMapping = tcDeptVos.stream().collect(Collectors.toMap(TcDept::getName, TcDept::getId));
//        //获取用户
//        TcUserService userService = SpringBeanUtil.getBeanByClass(TcUserService.class);
//        List<TcUser> tcUsers = userService.selectByList(new QueryParams<>());
//        Map<String, String> reverseUserDicMapping = tcUsers.stream().collect(Collectors.toMap(TcUser::getNickName, TcUser::getId));

        for (DictQuery query:dictQueryList){
            String dictQueryType = query.getDictQueryType();
            if (StrUtil.isBlank(dictQueryType)){
                throw new CommonException("字典查询类型不能为空");
            }
            String dictMappingKey = query.getDictMappingKey();
            if (StrUtil.isBlank(dictMappingKey)){
                throw new CommonException("字典映射编码不能为空");
            }
            String dictGroupCode = query.getDictGroupCode();
            if (StrUtil.isBlank(dictGroupCode)){
                throw new CommonException("字典组编码不能为空");
            }

            if (DictQueryTypeEnum.COMMON.name().equals(dictQueryType)){
                Map<String, String> reverseDictMapping = dictService.getReverseDictByGroupKey(dictGroupCode);
                result.put(dictMappingKey,reverseDictMapping);
            }

            if (DictQueryTypeEnum.TREE.name().equals(dictQueryType)){
                Map<String, String> reverseDictMapping = dictService.getReverseDictByGroupKey(dictGroupCode);
                result.put(dictMappingKey,reverseDictMapping);
            }

//            if (DictQueryTypeEnum.DEPARTMENT_COMMON.name().equals(dictQueryType)){
//                result.put(dictMappingKey,reverseDeptDicMapping);
//            }
//
//            if (DictQueryTypeEnum.DEPARTMENT_TREE.name().equals(dictQueryType)){
//                result.put(dictMappingKey,reverseDeptDicMapping);
//            }
//
//            if (DictQueryTypeEnum.USER_COMMON.name().equals(dictQueryType)){
//                result.put(dictMappingKey,reverseUserDicMapping);
//            }
        }
        return  result;
    }

    @Override
    @Cacheable(value = getDictStrMappingByQuery,keyGenerator = CustomCacheConfig.keyGeneratorName)
    public Map<String, Map<String, String>> getDictStrMappingByQuery(List<DictQuery> dictQueryList) {
        Map<String,Map<String,String>> result=new HashMap<>();
        if (CollectionUtil.isEmpty(dictQueryList)){
            return  result;
        }
        TcDictService dictService = SpringBeanUtil.getBeanByClass(TcDictService.class);
        //获取部门人员
        List<TcDeptVo> tcDeptVos = deptService.selectAllByList(new QueryData<>());
        Map<String, String> deptDicMapping = tcDeptVos.stream().collect(Collectors.toMap(TcDept::getId, TcDept::getName));
        //获取用户
        TcUserService userService = SpringBeanUtil.getBeanByClass(TcUserService.class);
        List<TcUser> tcUsers = userService.selectByList(new QueryData<>());
        Map<String, String> userDicMapping = tcUsers.stream().collect(Collectors.toMap(TcUser::getId, TcUser::getNickName));

        for (DictQuery query:dictQueryList){
            String dictQueryType = query.getDictQueryType();
            if (StrUtil.isBlank(dictQueryType)){
                throw new CommonException("字典查询类型不能为空");
            }
            String dictMappingKey = query.getDictMappingKey();
            if (StrUtil.isBlank(dictMappingKey)){
                throw new CommonException("字典映射编码不能为空");
            }
            String dictGroupCode = query.getDictGroupCode();
            if (StrUtil.isBlank(dictGroupCode)){
                throw new CommonException("字典组编码不能为空");
            }

            if (DictQueryTypeEnum.COMMON.name().equals(dictQueryType)){
                Map<String, String> dictMapping = dictService.getDictByGroupKey(dictGroupCode);
                result.put(dictMappingKey,dictMapping);
            }

            if (DictQueryTypeEnum.TREE.name().equals(dictQueryType)){
                Map<String, String> dictMapping = dictService.getDictByGroupKey(dictGroupCode);
                result.put(dictMappingKey,dictMapping);
            }

            if (DictQueryTypeEnum.DEPARTMENT_COMMON.name().equals(dictQueryType)){
                result.put(dictMappingKey,deptDicMapping);
            }

            if (DictQueryTypeEnum.DEPARTMENT_TREE.name().equals(dictQueryType)){
                result.put(dictMappingKey,deptDicMapping);
            }

            if (DictQueryTypeEnum.USER_COMMON.name().equals(dictQueryType)){
                result.put(dictMappingKey,userDicMapping);
            }
        }
        return  result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {selectByPage,getDictByGroupKey,getReverseDictByGroupKey,getDictListByGroupKey
            ,getDictMappingByQuery,getReverseDictStrMappingByQuery,getDictStrMappingByQuery}, allEntries = true)
    public String deleteData(List<TcDict> list) {
        List<String> idList = list.stream().map(TcDict::getId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(idList)){
            //删除
            LambdaQueryWrapper<TcDict> parentWrapper=new LambdaQueryWrapper<>();
            parentWrapper.in(TcDict::getId,idList);
            TcDict parent=new TcDict();
            parent.setJudgeEnable(0);
            dictMapper.update(parent,parentWrapper);

            //删除子项
            parentWrapper.clear();
            parentWrapper.in(TcDict::getParentId,idList);
            List<TcDict> childrenList = dictMapper.selectList(parentWrapper);
            if (CollectionUtil.isNotEmpty(childrenList)){
                deleteData(childrenList);
            }
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {selectByPage,getDictByGroupKey,getReverseDictByGroupKey,getDictListByGroupKey
            ,getDictMappingByQuery,getReverseDictStrMappingByQuery,getDictStrMappingByQuery}, allEntries = true)
    public String saveOrUpdateData(SaveOrUpdateDictDto dictDto) {
        TcDictGroup parentDictGroup=dictDto.getParentDictGroup();
        if (parentDictGroup==null){
            throw new CommonException("字典组不能为空");
        }
        if (StrUtil.isBlank(parentDictGroup.getGroupKey())){
            throw new CommonException("字典组key不能为空");
        }
        List<TcDict> list= dictDto.getChildrenList();
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("新增修改数据不能为空");
        }

        TcDict parentDict = dictDto.getParentDict();

        ValidationUtils.validateEntityListIgnore(list, Arrays.asList("id","judgeEnable","groupKey","level"));
        LambdaQueryWrapper<TcDict> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        Date now = new Date();
        for (TcDict item:list){
            item.setJudgeEnable(1);
            item.setGroupKey(parentDictGroup.getGroupKey());

            if (parentDict!=null){
                String parentId = parentDict.getId();
                if (StrUtil.isBlank(parentId)){
                    throw new CommonException("父项id不存在，请保存父项字典再保存子项字典");
                }
                Integer parentLevel = parentDict.getLevel();
                if (parentLevel==null){
                    throw new CommonException("父项层级不存在");
                }
                item.setLevel(parentLevel+1);
                item.setParentId(parentId);
            }else {
                item.setLevel(0);
                item.setParentId("0");
            }

            //判断是否存在
            lambdaQueryWrapper.clear();
            lambdaQueryWrapper
                    .eq(TcDict::getGroupKey,item.getGroupKey())
                    .eq(TcDict::getDictCode,item.getDictCode())
                    .eq(TcDict::getParentId,item.getParentId())
                    .eq(TcDict::getJudgeEnable,1);
            List<TcDict> dictList=dictMapper.selectList(lambdaQueryWrapper);


            if (CollectionUtil.isEmpty(dictList)){
                //插入
                item.setId(SnowIdUtil.getSnowId());
                item.setCreateDate(now);
                dictMapper.insert(item);
            }else {
                if (dictList.size()!=1){
                    throw new CommonException(StrUtil.format("parentId->{},字典组->{},字典编码->{}存在多个"
                            ,item.getParentId(),item.getGroupKey(),item.getDictCode()));
                }
                item.setId(dictList.get(0).getId());
                item.setUpdateDate(now);
                item.setJudgeEnable(1);
                dictMapper.updateById(item);
            }
        }
        return "操作成功";
    }


    @Override
    public TcDictMapper getMapper() {
        return dictMapper;
    }
}

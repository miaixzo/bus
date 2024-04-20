package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.lang.tree.NodeConfig;
import org.miaixz.bus.core.lang.tree.Tree;
import org.miaixz.bus.core.lang.tree.TreeBuilder;
import org.miaixz.bus.core.lang.tree.TreeNode;
import org.miaixz.bus.core.lang.tree.parser.DefaultNodeParser;
import org.miaixz.bus.core.lang.tree.parser.NodeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 树工具类
 * 提供通用树生成，特点：
 * 1、每个字段可自定义
 * 2、支持排序 树深度配置,自定义转换器等
 * 3、支持额外属性扩展
 * 4、贴心 许多属性,特性都有默认值处理
 * 5、使用简单 可一行代码生成树
 * 6、代码简洁轻量无额外依赖
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TreeKit {

    /**
     * 构建单root节点树
     *
     * @param list 源数据集合
     * @return {@link Tree}
     */
    public static Tree<Integer> buildSingle(List<TreeNode<Integer>> list) {
        return buildSingle(list, 0);
    }

    /**
     * 树构建
     *
     * @param list 源数据集合
     * @return List
     */
    public static List<Tree<Integer>> build(List<TreeNode<Integer>> list) {
        return build(list, 0);
    }

    /**
     * 构建单root节点树
     *
     * @param <E>      ID类型
     * @param list     源数据集合
     * @param parentId 最顶层父id值 一般为 0 之类
     * @return {@link Tree}
     */
    public static <E> Tree<E> buildSingle(List<TreeNode<E>> list, E parentId) {
        return buildSingle(list, parentId, NodeConfig.DEFAULT_CONFIG, new DefaultNodeParser<>());
    }

    /**
     * 树构建
     *
     * @param <E>      ID类型
     * @param list     源数据集合
     * @param parentId 最顶层父id值 一般为 0 之类
     * @return List
     */
    public static <E> List<Tree<E>> build(List<TreeNode<E>> list, E parentId) {
        return build(list, parentId, NodeConfig.DEFAULT_CONFIG, new DefaultNodeParser<>());
    }

    /**
     * 构建单root节点树
     *
     * @param <T>        转换的实体 为数据源里的对象类型
     * @param <E>        ID类型
     * @param list       源数据集合
     * @param parentId   最顶层父id值 一般为 0 之类
     * @param nodeParser 转换器
     * @return {@link Tree}
     */
    public static <T, E> Tree<E> buildSingle(List<T> list, E parentId, NodeParser<T, E> nodeParser) {
        return buildSingle(list, parentId, NodeConfig.DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 树构建
     *
     * @param <T>        转换的实体 为数据源里的对象类型
     * @param <E>        ID类型
     * @param list       源数据集合
     * @param parentId   最顶层父id值 一般为 0 之类
     * @param nodeParser 转换器
     * @return the list
     */
    public static <T, E> List<Tree<E>> build(List<T> list, E parentId, NodeParser<T, E> nodeParser) {
        return build(list, parentId, NodeConfig.DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 树构建
     *
     * @param <T>        转换的实体 为数据源里的对象类型
     * @param <E>        ID类型
     * @param list       源数据集合
     * @param rootId     最顶层父id值 一般为 0 之类
     * @param nodeConfig 配置
     * @param nodeParser 转换器
     * @return the list
     */
    public static <T, E> List<Tree<E>> build(List<T> list, E rootId, NodeConfig nodeConfig, NodeParser<T, E> nodeParser) {
        return buildSingle(list, rootId, nodeConfig, nodeParser).getChildren();
    }

    /**
     * 构建单root节点树
     *
     * @param <T>        转换的实体 为数据源里的对象类型
     * @param <E>        ID类型
     * @param list       源数据集合
     * @param rootId     最顶层父id值 一般为 0 之类
     * @param nodeConfig 配置
     * @param nodeParser 转换器
     * @return {@link Tree}
     */
    public static <T, E> Tree<E> buildSingle(List<T> list, E rootId, NodeConfig nodeConfig, NodeParser<T, E> nodeParser) {
        return TreeBuilder.of(rootId, nodeConfig).append(list, rootId, nodeParser).build();
    }

    /**
     * 树构建，按照权重排序
     *
     * @param <E>    ID类型
     * @param map    源数据Map
     * @param rootId 最顶层父id值 一般为 0 之类
     * @return the list
     */
    public static <E> List<Tree<E>> build(Map<E, Tree<E>> map, E rootId) {
        return buildSingle(map, rootId).getChildren();
    }

    /**
     * 单点树构建，按照权重排序
     *
     * @param <E>    ID类型
     * @param map    源数据Map
     * @param rootId 根节点id值 一般为 0 之类
     * @return {@link Tree}
     */
    public static <E> Tree<E> buildSingle(Map<E, Tree<E>> map, E rootId) {
        final Tree<E> tree = IterKit.getFirstNoneNull(map.values());
        if (null != tree) {
            final NodeConfig config = tree.getConfig();
            return TreeBuilder.of(rootId, config)
                    .append(map)
                    .build();
        }

        return createEmptyNode(rootId);
    }

    /**
     * 获取ID对应的节点，如果有多个ID相同的节点，只返回第一个。
     * 此方法只查找此节点及子节点，采用递归深度优先遍历。
     *
     * @param <T>  ID类型
     * @param node 节点
     * @param id   ID
     * @return 节点
     */
    public static <T> Tree<T> getNode(Tree<T> node, T id) {
        if (ObjectKit.equals(id, node.getId())) {
            return node;
        }

        final List<Tree<T>> children = node.getChildren();
        if (null == children) {
            return null;
        }

        // 查找子节点
        Tree<T> childNode;
        for (Tree<T> child : children) {
            childNode = child.getNode(id);
            if (null != childNode) {
                return childNode;
            }
        }

        // 未找到节点
        return null;
    }

    /**
     * 获取所有父节点名称列表
     *
     * <p>
     * 比如有个人在研发1部，他上面有研发部，接着上面有技术中心
     * 返回结果就是：[研发一部, 研发中心, 技术中心]
     *
     * @param <T>                节点ID类型
     * @param node               节点
     * @param includeCurrentNode 是否包含当前节点的名称
     * @return 所有父节点名称列表，node为null返回空List
     */
    public static <T> List<CharSequence> getParentsName(Tree<T> node, boolean includeCurrentNode) {
        final List<CharSequence> result = new ArrayList<>();
        if (null == node) {
            return result;
        }

        if (includeCurrentNode) {
            result.add(node.getName());
        }

        Tree<T> parent = node.getParent();
        while (null != parent) {
            result.add(parent.getName());
            parent = parent.getParent();
        }
        return result;
    }

    /**
     * 创建空Tree的节点
     *
     * @param id  节点ID
     * @param <E> 节点ID类型
     * @return {@link Tree}
     */
    public static <E> Tree<E> createEmptyNode(E id) {
        return new Tree<E>().setId(id);
    }

}

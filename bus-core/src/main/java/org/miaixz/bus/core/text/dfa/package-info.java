/**
 * DFA全称为：Deterministic Finite Automaton,即确定有穷自动机
 * 用所有关键字构造一棵树，然后用正文遍历这棵树，遍历到叶子节点即表示文章中存在这个关键字。
 * 我们暂且忽略构建关键词树的时间，每次查找正文只需要O(n)复杂度就可以搞定。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.core.text.dfa;

package org.ukettle.basics.shiro.extend.realm;

import javax.annotation.PostConstruct;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ukettle.basics.shiro.entity.Shiro;
import org.ukettle.basics.shiro.extend.authc.UsernamePassword2Token;
import org.ukettle.engine.loader.BasicService;
import org.ukettle.system.entity.User;
import org.ukettle.www.toolkit.Constant;
import org.ukettle.www.toolkit.Encode;
import org.ukettle.www.toolkit.Random;

@Service
public class Authorizing2Realm extends AuthorizingRealm {

	@Autowired
	public BasicService service;

	/**
	 * 认证回调函数,登录时调用.
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken authcToken) throws AuthenticationException {
		UsernamePassword2Token token = (UsernamePassword2Token) authcToken;
		String username = token.getUsername();
		if (username == null || null == username) {
			throw new AccountException(
					"Null usernames are not allowed by this realm.");
		}
		User entity = new User();
		entity.setEmail(username);
		entity.setStatus(Constant.STATUS_ENABLED);
		entity = (User) service.iUserService.select(entity);
		if (null == entity) {
			throw new UnknownAccountException("No account found for user ["
					+ username + "]");
		}
		byte[] key = Encode.decodeHex(entity.getRandom());
		return new SimpleAuthenticationInfo(new Shiro(entity.getId(),
				entity.getEmail(), entity.getName()), entity.getPassword(),
				ByteSource.Util.bytes(key), getName());
	}

	/**
	 * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principalCollection) {
		if (principalCollection == null) {
			throw new AuthorizationException("Principal is not null!");
		}
		Shiro shiro = (Shiro) principalCollection.getPrimaryPrincipal();
		User entity = new User();
		entity.setId(shiro.getId());
		entity = (User) service.iUserService.select(entity);
		if (null == entity) {
			throw new UnknownAccountException("No account found for user ["
					+ shiro.getId() + "]");
		}
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		return info;
	}

	/**
	 * 更新用户授权信息缓存.
	 */
	public void clearCachedAuthorizationInfo(String principal) {
		SimplePrincipalCollection principals = new SimplePrincipalCollection(
				principal, getName());
		clearCachedAuthorizationInfo(principals);
	}

	/**
	 * 清除所有用户授权信息缓存.
	 */
	public void clearAllCachedAuthorizationInfo() {
		Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
		if (cache != null) {
			for (Object key : cache.keys()) {
				cache.remove(key);
			}
		}
	}

	@PostConstruct
	public void initCredentialsMatcher() {
		HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(
				Random.SHA1);
		matcher.setHashIterations(Random.ITERATION);
		setCredentialsMatcher(matcher);
	}

}
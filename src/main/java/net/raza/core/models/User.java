package net.raza.core.models;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.raza.core.enums.RoleEnum;
import net.raza.core.security.Authority;
import net.raza.core.security.RoleAuthorities;

/**
 * 
 * User entity. Relative to every person allowed to use and/or manage the system
 * 
 * @author tazabreu
 *
 */

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity<Long> implements UserDetails, CredentialsContainer {
	
	public final static Long SYSTEM_ID = 1L;
	
    
    /** ---------------------- Entity Attributes ---------------- **/

	private static final long serialVersionUID = 871861820518861281L;

	/** The user login */
    private String username;
    
    /** The email. */
    private String email;
    
    /** The password. */
    private String password;
    
    /** The role. */
    @Enumerated(EnumType.STRING)
    private RoleEnum role;
    
    public User() {
    	
    	this.role = RoleEnum.USER;
    	this.enabled = true;
    	this.accountNonExpired = true;
    	this.accountNonLocked = true;
    	this.credentialsNonExpired = true;
    	this.authorities = Collections.unmodifiableSet(sortAuthorities(RoleAuthorities.getAuthorities(this.role)));
    	
    }
    
    
    /** ---------------------- Attributes for Spring Security ---------------- **/
    @Transient
	private final Set<Authority> authorities;
    
    @Transient
	private final boolean accountNonExpired;
	
    @Transient
	private final boolean accountNonLocked;
	
    @Transient
	private final boolean credentialsNonExpired;
	
	private final boolean enabled;
    
    /** ---------------------- Methods for Spring Security ---------------- **/

	@Override
	public void eraseCredentials() {
		this.password = null;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
	
	/** ---------------------- Constructors for Spring Security ---------------- **/
	

	/**
	 * Calls the more complex constructor with all boolean arguments set to {@code true}.
	 */
	public User(String username, String password,
			Collection<? extends GrantedAuthority> authorities) {
		this(username, password, true, true, true, true, authorities);
	}

	/**
	 * Construct the <code>User</code> with the details required by
	 * {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}.
	 *
	 * @param username the username presented to the
	 * <code>DaoAuthenticationProvider</code>
	 * @param password the password that should be presented to the
	 * <code>DaoAuthenticationProvider</code>
	 * @param enabled set to <code>true</code> if the user is enabled
	 * @param accountNonExpired set to <code>true</code> if the account has not expired
	 * @param credentialsNonExpired set to <code>true</code> if the credentials have not
	 * expired
	 * @param accountNonLocked set to <code>true</code> if the account is not locked
	 * @param authorities the authorities that should be granted to the caller if they
	 * presented the correct username and password and the user is enabled. Not null.
	 *
	 * @throws IllegalArgumentException if a <code>null</code> value was passed either as
	 * a parameter or as an element in the <code>GrantedAuthority</code> collection
	 */
	public User(String username, String password, boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {

		if (((username == null) || "".equals(username)) || (password == null)) {
			throw new IllegalArgumentException(
					"Cannot pass null or empty values to constructor");
		}

		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.accountNonExpired = accountNonExpired;
		this.credentialsNonExpired = credentialsNonExpired;
		this.accountNonLocked = accountNonLocked;
		this.role = RoleEnum.USER;
    	this.authorities = Collections.unmodifiableSet(sortAuthorities(RoleAuthorities.getAuthorities(this.role)));
	}
	
	
    /** ---------------------- Specific Methods ---------------- **/
	
	private static SortedSet<Authority> sortAuthorities(
			Collection<Authority> authorities) {
		Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
		// Ensure array iteration order is predictable (as per
		// UserDetails.getAuthorities() contract and SEC-717)
		SortedSet<Authority> sortedAuthorities = new TreeSet<Authority>(
				new AuthorityComparator());
		
		for (Authority authority : authorities) {
			Assert.notNull(authority,
					"GrantedAuthority list cannot contain any null elements");
			sortedAuthorities.add(authority);
		}
		
		return sortedAuthorities;
	}
	
	private static class AuthorityComparator implements Comparator<GrantedAuthority>,
	Serializable {
		private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
		
		public int compare(GrantedAuthority g1, GrantedAuthority g2) {
			// Neither should ever be null as each entry is checked before adding it to
			// the set.
			// If the authority is null, it is a custom authority and should precede
			// others.
			if (g2.getAuthority() == null) {
				return -1;
			}
			
			if (g1.getAuthority() == null) {
				return 1;
			}
			
			return g1.getAuthority().compareTo(g2.getAuthority());
		}
	}

	/**
	 * Returns {@code true} if the supplied object is a {@code User} instance with the
	 * same {@code username} value.
	 * <p>
	 * In other words, the objects are equal if they have the same username, representing
	 * the same principal.
	 */
	@Override
	public boolean equals(Object rhs) {
		if (rhs instanceof User) {
			return username.equals(((User) rhs).username);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append(": ");
		sb.append("Username: ").append(this.username).append("; ");
		sb.append("Password: [PROTECTED]; ");
		sb.append("Enabled: ").append(this.enabled).append("; ");
		sb.append("AccountNonExpired: ").append(this.accountNonExpired).append("; ");
		sb.append("credentialsNonExpired: ").append(this.credentialsNonExpired)
				.append("; ");
		sb.append("AccountNonLocked: ").append(this.accountNonLocked).append("; ");

		if (!authorities.isEmpty()) {
			sb.append("Granted Authorities: ");

			boolean first = true;
			for (GrantedAuthority auth : authorities) {
				if (!first) {
					sb.append(",");
				}
				first = false;

				sb.append(auth);
			}
		}
		else {
			sb.append("Not granted any authorities");
		}

		return sb.toString();
	}
	
}

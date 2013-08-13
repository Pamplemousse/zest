/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.zest.core.v1;

import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class ZestExpressionRegex.
 */
public class ZestExpressionRegex extends ZestExpression{
	
	/** The regex. */
	private String regex;
	
	/** The variableName. */
	private String variableName;
	
	/** The inverse. */
	private boolean inverse=false;
	
	/** The pattern. */
	private transient Pattern pattern = null;

	/**
	 * Instantiates a new zest expression regex.
	 */
	public ZestExpressionRegex(){
		this("", null, false);
	}
	
	/**
	 * Instantiates a new zest expression regex.
	 *
	 * @param variableName the variableName
	 * @param regex the regex
	 */
	public ZestExpressionRegex(String variableName, String regex) {
		this(variableName, regex, false);
	}
	
	/**
	 * Instantiates a new zest expression regex.
	 *
	 * @param variableName the variableName
	 * @param regex the regex
	 * @param inverse the inverse
	 */
	public ZestExpressionRegex(String variableName, String regex, boolean inverse) {
		super ();
		this.inverse=inverse;
		this.variableName = variableName;
		this.regex = regex;
		if (regex != null) {
			this.pattern = Pattern.compile(regex);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mozilla.zest.core.v1.ZestExpressionElement#isTrue(org.mozilla.zest.core.v1.ZestResponse)
	 */
	public boolean isTrue (ZestRuntime runtime) {
		ZestResponse response = runtime.getLastResponse();
		if (response == null) {
			return false;
		}
		String str = runtime.getVariable(variableName);		
		if (str == null) {
			return false;
		}
		if (pattern == null && regex != null) {
			this.pattern = Pattern.compile(regex);
		}
		
		return pattern.matcher(str).find();
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	/**
	 * Gets the regex.
	 *
	 * @return the regex
	 */
	public String getRegex() {
		return regex;
	}

	/**
	 * Sets the regex.
	 *
	 * @param regex the new regex
	 */
	public void setRegex(String regex) {
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
	}

	/* (non-Javadoc)
	 * @see org.mozilla.zest.core.v1.ZestExpression#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		return true;
	}


	/* (non-Javadoc)
	 * @see org.mozilla.zest.core.v1.ZestExpression#isInverse()
	 */
	@Override
	public boolean isInverse() {
		return inverse;
	}

	/* (non-Javadoc)
	 * @see org.mozilla.zest.core.v1.ZestExpression#setInverse(boolean)
	 */
	@Override
	public void setInverse(boolean not) {
		inverse=not;
	}
	
	/* (non-Javadoc)
	 * @see org.mozilla.zest.core.v1.ZestExpression#deepCopy()
	 */
	@Override
	public ZestExpressionRegex deepCopy() {
		return new ZestExpressionRegex(this.getVariableName(), this.getRegex(), this.isInverse());
	}
	
}

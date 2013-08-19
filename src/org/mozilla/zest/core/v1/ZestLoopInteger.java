/**
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * @author Alessandro Secco: seccoale@gmail.com
 */
package org.mozilla.zest.core.v1;

import java.util.LinkedList;

/**
 * This class represent a loop through a set of integers.
 */
public class ZestLoopInteger extends ZestLoop<Integer> {
	
	private ZestLoopTokenIntegerSet set;
	/**
	 * Instantiates a new zest loop integer.
	 */
	public ZestLoopInteger(){
		super( );
		this.set=new ZestLoopTokenIntegerSet();
		super.init(getSet(), new LinkedList<ZestStatement>());
	}
	
	/**
	 * Instantiates a new zest loop integer.
	 *
	 * @param name the name
	 */
	public ZestLoopInteger(String name){
		super();
		super.setVariableName(name);
		this.set=new ZestLoopTokenIntegerSet();
		super.init(set, new LinkedList<ZestStatement>());
	}
	
	/**
	 * Instantiates a new zest loop integer.
	 *
	 * @param name the name
	 * @param start the start
	 * @param end the end
	 */
	public ZestLoopInteger(String name, int start, int end){
		super();
		super.setVariableName(name);
		this.set=new ZestLoopTokenIntegerSet(start, end);
		super.init(set, new LinkedList<ZestStatement>());
	}
	
	/**
	 * Instantiates a new zest loop integer.
	 *
	 * @param index the index
	 * @param start the start
	 * @param end the end
	 */
	public ZestLoopInteger(int index, int start, int end){
		super(index);
		this.set=new ZestLoopTokenIntegerSet(start, end);
		super.init(set, new LinkedList<ZestStatement>());
	}
	
	/**
	 * Gets the start.
	 *
	 * @return the start
	 */
	public int getStart(){
		return this.getSet().getStart();
	}
	
	/**
	 * Gets the end.
	 *
	 * @return the end
	 */
	public int getEnd(){
		return this.getSet().getEnd();
	}
	
	/**
	 * Instantiates a new zest loop integer.
	 *
	 * @param start the start
	 * @param end the end
	 */
	public ZestLoopInteger(int start, int end){
		this("", start, end);
	}
	
	@Override
	public ZestLoopStateInteger getCurrentState(){
		return (ZestLoopStateInteger)super.getCurrentState();
	}
	
	public void setStep(int step){
		this.getSet().setStep(step);
	}
	
	public int getStep(){
		return this.getSet().getStep();
	}
	
	@Override
	public ZestLoopInteger deepCopy(){
		ZestLoopStateInteger state=this.getCurrentState().deepCopy();
		ZestLoopTokenIntegerSet set=this.getSet();
		ZestLoopInteger copy=new ZestLoopInteger(set.getStart(), set.getEnd());
		for(ZestStatement stmt:this.getStatements()){
			copy.addStatement(stmt.deepCopy());
		}
		copy.setCurrentState(state);
		return copy;
	}

	@Override
	public ZestLoopTokenIntegerSet getSet() {
		return this.set;
	}
	@Override
	public void setSet(ZestLoopTokenSet<Integer> newSet){
		super.setSet(newSet);
		this.set=(ZestLoopTokenIntegerSet)newSet;
	}

	@Override
	public boolean isLastState() {
		return getCurrentState().getCurrentIndex()>=this.getSet().size();
	}

	@Override
	public void increase() {
		this.getCurrentState().increase(getSet());
	}

	@Override
	public void toLastState() {
		this.getCurrentState().toLastState(getSet());
	}
	
	public boolean loop(){
		return super.loop(getSet());
	}
	public void endLoop(){
		this.endLoop(getSet());
	}
	public void setStart(int newStart){
		this.setSet(new ZestLoopTokenIntegerSet(newStart, getEnd()));
	}
	public void setEnd(int newEnd){
		this.setSet(new ZestLoopTokenIntegerSet(this.getStart(), newEnd));
	}
	
}
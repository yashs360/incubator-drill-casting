package org.apache.drill.exec.resolver;

import java.util.List;

import org.apache.drill.common.expression.FunctionCall;
import org.apache.drill.exec.expr.fn.DrillFuncHolder;

public class DefaultFunctionResolver implements FunctionResolver {

	@Override
	public DrillFuncHolder getBestMatch(List<DrillFuncHolder> methods,FunctionCall call) {
		for(DrillFuncHolder h : methods){
		   	   if(h.matches(call)){
		   	     	return h;
		   	   }
		 }
		
		return null;

	}

}

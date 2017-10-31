package com.webcohesion.enunciate.modules.freemarker;

import java.util.List;

import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Template method model wrapper to allow typed implementations.
 * @author jb1811
 *
 * @param <R> Return type
 * @param <S> Argument type
 */
public abstract class Typed1ArgTemplateMethod<R, S> implements TemplateMethodModelEx {
  private Class<S> type1;

  protected Typed1ArgTemplateMethod(Class<S> type1) {
    this.type1 = type1;
  }
  
  @Override
  public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
    return exec(getArg(arguments, 0, type1));
  }
  
  protected abstract R exec(S a);

  public static <T> T getArg(@SuppressWarnings("rawtypes") List paramList, int index, Class<T> type) throws TemplateModelException {
    if (index >= paramList.size()) {
      throw new TemplateModelException("The exec method requires argument type " + type + " at index " + index);
    }

    Object obj = paramList.get(0);
    if (obj instanceof TemplateModel) {
      obj = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build().unwrap((TemplateModel)obj);
    }
    
    if (obj == null || !type.isAssignableFrom(obj.getClass())) {
      String actualType = obj == null ? "null" : obj.getClass().getName();
      throw new TemplateModelException("The exec method requires argument type " + type
                                     + " at index " + index + ", got " + actualType + ": " + obj);
    }
    return type.cast(obj);
  }
}

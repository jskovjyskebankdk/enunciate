package com.webcohesion.enunciate.modules.jackson.javac;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.util.Collections;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ParameterizedJacksonDeclaredType extends DecoratedDeclaredType {

  private final DeclaredType root;
  private final ParameterizedJacksonTypeElement element;

  public ParameterizedJacksonDeclaredType(DeclaredType root, EnunciateContext context) {
    super(root, context.getProcessingEnvironment());
    this.root = root;
    this.element = new ParameterizedJacksonTypeElement(this.root, context);
  }

  @Override
  public Element asElement() {
    return this.element;
  }

  @Override
  public TypeMirror getEnclosingType() {
    return root.getEnclosingType();
  }

  @Override
  public List<? extends TypeMirror> getTypeArguments() {
    return Collections.emptyList();
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.DECLARED;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitDeclared(this, p);
  }
}

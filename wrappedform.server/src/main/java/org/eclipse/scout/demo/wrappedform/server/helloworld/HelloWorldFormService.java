package org.eclipse.scout.demo.wrappedform.server.helloworld;

import org.eclipse.scout.demo.wrappedform.server.ServerSession;
import org.eclipse.scout.demo.wrappedform.shared.helloworld.HelloWorldFormData;
import org.eclipse.scout.demo.wrappedform.shared.helloworld.IHelloWorldFormService;

/**
 * <h3>{@link HelloWorldFormService}</h3>
 *
 * @author sgr
 */
public class HelloWorldFormService implements IHelloWorldFormService {

  @Override
  public HelloWorldFormData load(HelloWorldFormData input) {
    StringBuilder msg = new StringBuilder();
    msg.append("Hello ").append(ServerSession.get().getUserId()).append("!");
    input.getMessage().setValue(msg.toString());
    return input;
  }
}

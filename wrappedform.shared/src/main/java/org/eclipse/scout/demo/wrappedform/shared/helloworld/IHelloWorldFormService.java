package org.eclipse.scout.demo.wrappedform.shared.helloworld;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.TunnelToServer;

import org.eclipse.scout.demo.wrappedform.shared.helloworld.HelloWorldFormData;

/**
 * <h3>{@link IHelloWorldFormService}</h3>
 *
 * @author sgr
 */
@TunnelToServer
public interface IHelloWorldFormService extends IService {
      HelloWorldFormData load(HelloWorldFormData input);
}

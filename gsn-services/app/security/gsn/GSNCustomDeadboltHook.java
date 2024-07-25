/** 
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*/
package security.gsn;

import javax.inject.Singleton;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import be.objectify.deadbolt.java.cache.HandlerCache;

public class GSNCustomDeadboltHook extends Module {
	
	@Override
	public Seq<Binding<?>> bindings(final Environment environment, final Configuration configuration) {
		return seq(bind(HandlerCache.class).to(GSNHandlerCache.class).in(Singleton.class));
	}
	
}
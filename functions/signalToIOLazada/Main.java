
import com.catalyst.Context;
import com.catalyst.event.EVENT_STATUS;
import com.catalyst.event.EventRequest;
import com.catalyst.event.CatalystEventHandler;
import com.zc.common.ZCProject;
import com.zc.component.cache.ZCCache;
import java.util.logging.Level;
import java.util.logging.Logger;




public class Main implements CatalystEventHandler{
	
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

	@Override
	public EVENT_STATUS handleEvent(EventRequest paramEventRequest, Context paramContext) throws Exception {
		
		try
		{
			ZCProject.initProject();

			Object rawData = paramEventRequest.getRawData();
			LOGGER.log(Level.SEVERE,"Raw Data is "+rawData.toString());

		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in Main Function",e);
		}
		
		return EVENT_STATUS.SUCCESS;
	}

}

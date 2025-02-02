package org.bonitasoft.web.rest.server.datastore.bpm.cases;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.bonitasoft.test.toolkit.bpm.TestCase;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseItem;
import org.bonitasoft.web.rest.server.AbstractConsoleTest;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.junit.Test;

/**
 * @author ROHART Bastien
 * 
 */
public class ArchivedCaseDatastoreIntegrationTest extends AbstractConsoleTest {

    private ArchivedCaseDatastore archivedCaseDatastore;

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.console.server.AbstractConsoleTest#consoleTestSetUp()
     */
    @Override
    public void consoleTestSetUp() throws Exception {
        archivedCaseDatastore = new ArchivedCaseDatastore(getInitiator().getSession());
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.test.toolkit.AbstractJUnitTest#getInitiator()
     */
    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test
    public void twoPoolsWithOneWithACallActivityArchivedCaseTest() throws Exception {
        TestProcess process1 = TestProcessFactory.getDefaultHumanTaskProcess();
        process1.addActor(getInitiator()).enable().startCase();

        // start process1 case via call activity
        TestProcess process2 = TestProcessFactory.getCallActivityProcess(process1.getProcessDefinition()); 
        process2.addActor(getInitiator()).enable().startCase();
        Thread.sleep(1000); // asynchronous, wait process1 to start
        
        // archive process 1 case
        TestCase testCaseProcess1 = process1.listOpenCases().get(0);
        testCaseProcess1.getNextHumanTask().assignTo(getInitiator()).archive();
        Thread.sleep(1000); // asynchronous, wait process2 to be archived

        // Filters for archived Cases
        ItemSearchResult<ArchivedCaseItem> itemSearchResult = archivedCaseDatastore.search(0, 100, null, null, new HashMap<>());

        assertEquals("2 cases started but one via call activity so only 1 should be archived", 1, itemSearchResult.getResults().size());

        TestProcessFactory.getInstance().delete(process2);
        TestProcessFactory.getInstance().delete(process1);

    }

}

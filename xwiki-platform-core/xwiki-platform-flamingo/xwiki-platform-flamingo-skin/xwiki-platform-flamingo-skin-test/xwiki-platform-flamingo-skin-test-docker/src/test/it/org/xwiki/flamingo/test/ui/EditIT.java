/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.flamingo.test.ui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.ViewPage;
import org.xwiki.test.ui.po.editor.WikiEditPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the wiki edit UI.
 *
 * @version $Id$
 * @since 11.2RC1
 */
@UITest
public class EditIT
{
    @BeforeAll
    public void setup(TestUtils setup)
    {
        setup.loginAsSuperAdmin();
    }

    /**
     * Test the ability to add edit comments and the ability to disable the edit comments feature, and verify.
     */
    @Test
    @Order(1)
    public void showAndHideEditComments(TestUtils setup, TestReference reference) throws Exception
    {
        ViewPage vp = setup.gotoPage(reference);

        // Verify that the edit comment field is there and that we can type in it.
        WikiEditPage wep = vp.editWiki();
        wep.setEditComment("some comment");
        wep.clickCancel();

        // Verify that we can disable the edit comment field
        // (Test for XWIKI-2487: Hiding the edit comment field doesn't work)
        try {
            setup.setPropertyInXWikiCfg("xwiki.editcomment.hidden=1");
            vp = setup.gotoPage(reference);
            wep = vp.editWiki();
            assertFalse(wep.isEditCommentDisplayed());
        } finally {
            setup.setPropertyInXWikiCfg("xwiki.editcomment.hidden=0");
        }
    }

    /**
     * Verify minor edit feature is working.
     */
    @Test
    @Order(2)
    public void minorEdit(TestUtils setup, TestReference reference)
    {
        setup.deletePage(reference);
        ViewPage vp = setup.gotoPage(reference);
        WikiEditPage wep = vp.editWiki();
        wep.setContent("version=1.1");

        // Save & Continue = minor edit.
        wep.clickSaveAndContinue();

        wep.setContent("version=2.1");

        // Save & View = major edit
        wep.clickSaveAndView();

        // Verify that the revision exists by navigating to it and by asserting its content
        setup.gotoPage(reference, "viewrev", "rev=2.1");

        vp = new ViewPage();
        assertEquals("version=2.1", vp.getContent());

        wep = vp.editWiki();
        wep.setContent("version=2.2");
        wep.setMinorEdit(true);
        wep.clickSaveAndView();

        // Verify that the minor revision exists by navigating to it and by asserting its content
        setup.gotoPage(reference, "viewrev", "rev=2.2");
        vp = new ViewPage();
        assertEquals("version=2.2", vp.getContent());
    }

    /**
     * Tests that users can completely remove the content from a document (make the document empty). In previous
     * versions (pre-1.5M2), removing all content in page had no effect. See XWIKI-1007.
     */
    @Test
    @Order(3)
    public void emptyDocumentContentIsAllowed(TestUtils setup, TestReference reference)
    {
        setup.deletePage(reference);
        setup.createPage(reference, "this is some content", "EmptyContentAllowed");
        ViewPage vp = setup.gotoPage(reference);
        WikiEditPage wep = vp.editWiki();
        wep.setContent("");
        vp = wep.clickSaveAndView();
        assertNull(ExpectedConditions.alertIsPresent().apply(setup.getDriver()));
        assertEquals(-1, setup.getDriver().getCurrentUrl().indexOf("/edit/"));
        assertEquals("", vp.getContent());
    }

    @Test
    @Order(4)
    public void emptyLineAndSpaceCharactersBeforeSectionTitleIsNotRemoved(TestUtils setup, TestReference reference)
    {
        setup.deletePage(reference);
        String content = "\n== Section ==\n\ntext";
        setup.createPage(reference, content, "Empty Line is not removed");
        ViewPage vp = setup.gotoPage(reference);
        WikiEditPage wep = vp.editWiki();
        assertEquals(content, wep.getExactContent());
    }

    @Test
    @Order(5)
    public void testBoldButton(TestUtils setup, TestReference reference)
    {
        testToolBarButton(setup, reference,"Bold", "**%s**", "Text in Bold");
        testToolBarButton(setup, reference,"Italics", "//%s//", "Text in Italics");
        testToolBarButton(setup, reference,"Underline", "__%s__", "Text in Underline");
        testToolBarButton(setup, reference,"Internal Link", "[[%s]]", "Link Example");
        testToolBarButton(setup, reference,"Horizontal ruler", "\n----\n", "");
        testToolBarButton(setup, reference,"Attached Image", "[[image:%s]]", "example.jpg");
    }

    /**
     * Tests that the specified tool bar button works.
     *
     * @param buttonTitle the title of a tool bar button
     * @param format the format of the text inserted by the specified button
     * @param defaultText the default text inserted if there's no text selected in the text area
     */
    private void testToolBarButton(TestUtils setup, TestReference reference, String buttonTitle,
        String format, String defaultText)
    {
        ViewPage vp = setup.gotoPage(reference);
        WikiEditPage wikiEditPage = vp.editWiki();
        wikiEditPage.clearContent();
        wikiEditPage.sendKeys("a");
        wikiEditPage.clickToolbarButton(buttonTitle);
        // Type b and c on two different lines and move the caret after b.
        wikiEditPage.sendKeys("b", Keys.ENTER, "c", Keys.ARROW_LEFT, Keys.ARROW_LEFT);
        wikiEditPage.clickToolbarButton(buttonTitle);
        // Move the caret after c, type d and e, then select d.
        wikiEditPage.sendKeys(Keys.PAGE_DOWN, Keys.END, "de", Keys.ARROW_LEFT);
        wikiEditPage.sendKeysWithAction(Keys.SHIFT, Keys.ARROW_LEFT);
        wikiEditPage.clickToolbarButton(buttonTitle);
        wikiEditPage = new WikiEditPage();
        if (defaultText.isEmpty()) {
            assertEquals("a" + format + "b" + format + "\nc" + format + "de", wikiEditPage.getExactContent());
        } else {
            assertEquals(
                String.format("a" + format + "b" + format + "\nc" + format + "e", defaultText, defaultText, "d"),
                wikiEditPage.getExactContent());
        }
    }
}

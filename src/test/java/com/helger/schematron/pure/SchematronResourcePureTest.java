/**
 * Copyright (C) 2014 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.schematron.pure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.junit.Test;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.helger.commons.charset.CCharset;
import com.helger.commons.io.IReadableResource;
import com.helger.commons.io.streams.StringInputStream;
import com.helger.commons.xml.serialize.DOMReader;
import com.helger.commons.xml.xpath.MapBasedXPathFunctionResolver;
import com.helger.commons.xml.xpath.MapBasedXPathVariableResolver;
import com.helger.schematron.SchematronException;
import com.helger.schematron.pure.errorhandler.CollectingPSErrorHandler;
import com.helger.schematron.svrl.SVRLUtils;
import com.helger.schematrontest.SchematronTestHelper;

/**
 * Test class for class {@link SchematronResourcePure}.
 *
 * @author Philip Helger
 */
public final class SchematronResourcePureTest
{
  @Test
  public void testBasic () throws Exception
  {
    for (final IReadableResource aRes : SchematronTestHelper.getAllValidSchematronFiles ())
    {
      // The validity is tested in another test case!
      // Parse them
      final SchematronResourcePure aResPure = new SchematronResourcePure (aRes);
      assertTrue (aRes.getPath (), aResPure.isValidSchematron ());
    }
  }

  @Test
  public void testFromByteArray ()
  {
    final String sTest = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
                         + "<iso:schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" \n"
                         + "         xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\" \n"
                         + "         xmlns:sch=\"http://www.ascc.net/xml/schematron\"\n"
                         + "         queryBinding='xslt2'\n"
                         + "         schemaVersion=\"ISO19757-3\">\n"
                         + "  <iso:title>Test ISO schematron file. Introduction mode</iso:title>\n"
                         + "  <iso:ns prefix=\"dp\" uri=\"http://www.dpawson.co.uk/ns#\" />\n"
                         + " <iso:pattern >\n"
                         + "    <iso:title>A very simple pattern with a title</iso:title>\n"
                         + "    <iso:rule context=\"chapter\">\n"
                         + "      <iso:assert test=\"title\">Chapter should have a title</iso:assert>\n"
                         + "      <iso:report test=\"count(para)\">\n"
                         + "      <iso:value-of select=\"count(para)\"/> paragraphs</iso:report>\n"
                         + "    </iso:rule>\n"
                         + "  </iso:pattern>\n"
                         + "\n"
                         + "</iso:schema>";
    assertTrue (SchematronResourcePure.fromByteArray (sTest.getBytes (CCharset.CHARSET_ISO_8859_1_OBJ))
                                      .isValidSchematron ());
    assertTrue (SchematronResourcePure.fromInputStream (new StringInputStream (sTest, CCharset.CHARSET_ISO_8859_1_OBJ))
                                      .isValidSchematron ());
  }

  @Test
  public void testParseWithXPathError ()
  {
    final String sTest = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
                         + "<iso:schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" \n"
                         + "         xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\" \n"
                         + "         xmlns:sch=\"http://www.ascc.net/xml/schematron\"\n"
                         + "         queryBinding='xslt2'\n"
                         + "         schemaVersion=\"ISO19757-3\">\n"
                         + "  <iso:title>Test ISO schematron file. Introduction mode</iso:title>\n"
                         + "  <iso:ns prefix=\"dp\" uri=\"http://www.dpawson.co.uk/ns#\" />\n"
                         + " <iso:pattern >\n"
                         + "    <iso:title>A very simple pattern with a title</iso:title>\n"
                         + "    <iso:rule context=\"chapter\">\n"
                         // This line contains the XPath error (Node xor number
                         // is invalid)
                         + "      <iso:assert test=\"title xor 55\">Chapter should have a title</iso:assert>\n"
                         + "      <iso:report test=\"count(para)\">\n"
                         + "      <iso:value-of select=\"count(para)\"/> paragraphs</iso:report>\n"
                         + "    </iso:rule>\n"
                         + "  </iso:pattern>\n"
                         + "\n"
                         + "</iso:schema>";
    final CollectingPSErrorHandler aErrorHandler = new CollectingPSErrorHandler ();
    assertFalse (SchematronResourcePure.fromByteArray (sTest.getBytes (CCharset.CHARSET_ISO_8859_1_OBJ))
                                       .setErrorHandler (aErrorHandler)
                                       .isValidSchematron ());
    assertEquals ("Expected only one error: " + aErrorHandler.getResourceErrors ().toString (),
                  1,
                  aErrorHandler.getResourceErrors ().size ());
    System.out.println (aErrorHandler.getResourceErrors ().toString ());
  }

  @Test
  public void testResolveVariables () throws SchematronException, SAXException
  {
    final String sTest = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
                         + "<iso:schema xmlns=\"http://purl.oclc.org/dsdl/schematron\" \n"
                         + "         xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\" \n"
                         + "         xmlns:sch=\"http://www.ascc.net/xml/schematron\"\n"
                         + "         queryBinding='xslt2'\n"
                         + "         schemaVersion=\"ISO19757-3\">\n"
                         + "  <iso:title>Test ISO schematron file. Introduction mode</iso:title>\n"
                         + "  <iso:ns prefix=\"dp\" uri=\"http://www.dpawson.co.uk/ns#\" />\n"
                         + "  <iso:ns prefix=\"java\" uri=\"http://helger.com/schematron/test\" />\n"
                         + "  <iso:pattern >\n"
                         + "    <iso:title>A very simple pattern with a title</iso:title>\n"
                         + "    <iso:rule context=\"chapter\">\n"
                         // Custom variable
                         + "      <iso:assert test=\"$title-element\">Chapter should have a title</iso:assert>\n"
                         // Custom function
                         + "      <iso:report test=\"java:my-count(para) = 2\">\n"
                         // Custom function
                         + "      <iso:value-of select=\"java:my-count(para)\"/> paragraphs found</iso:report>\n"
                         + "    </iso:rule>\n"
                         + "  </iso:pattern>\n"
                         + "\n"
                         + "</iso:schema>";

    // Test without variable and function resolver
    assertFalse (SchematronResourcePure.fromString (sTest, CCharset.CHARSET_ISO_8859_1_OBJ).isValidSchematron ());

    // Test with variable and function resolver
    final MapBasedXPathVariableResolver aVarResolver = new MapBasedXPathVariableResolver ();
    aVarResolver.addUniqueVariable ("title-element", "title");

    final MapBasedXPathFunctionResolver aFunctionResolver = new MapBasedXPathFunctionResolver ();
    aFunctionResolver.addUniqueFunction ("http://helger.com/schematron/test", "my-count", 1, new XPathFunction ()
    {
      public Object evaluate (@SuppressWarnings ("rawtypes") final List args) throws XPathFunctionException
      {
        final List <?> aArg = (List <?>) args.get (0);
        return Integer.valueOf (aArg.size ());
      }
    });
    final Document aTestDoc = DOMReader.readXMLDOM ("<?xml version='1.0'?><chapter><title /><para>First para</para><para>Second para</para></chapter>");
    final SchematronOutputType aOT = SchematronResourcePure.fromString (sTest, CCharset.CHARSET_ISO_8859_1_OBJ)
                                                           .setVariableResolver (aVarResolver)
                                                           .setFunctionResolver (aFunctionResolver)
                                                           .applySchematronValidation (aTestDoc);
    assertNotNull (aOT);
    assertEquals (0, SVRLUtils.getAllFailedAssertions (aOT).size ());
    assertEquals (1, SVRLUtils.getAllSuccesssfulReports (aOT).size ());
    // Note: the text contains all whitespaces!
    assertEquals ("\n      2 paragraphs found", SVRLUtils.getAllSuccesssfulReports (aOT).get (0).getText ());
  }
}
/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework;

import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Hashtable;

import java.util.StringTokenizer;

public class statusU {

    class TestData {
        private int status;
        private String desc;

        public TestData(int status, String desc) {
            this.status = status;
            this.desc = desc;
        }

        int getStatus() { return status; }
        String getDesc() { return desc; }

        public String toString() {
            StringBuffer sb = new StringBuffer();

            if (getStatus() == RTMConstants.PASS)
                sb.append(PASS);
            else
                sb.append(FAIL);

            sb.append(" - ");
            sb.append(getDesc());

            return sb.toString();
        }
    };

    /* FIELDS */

    private final String PASS = new String("PASSED");
    private final String FAIL = new String("FAILED");

    /**
     * TreeMap is used becuase it ensures that unique key-value pairs are
     * stored in data structure. It stores the keys in an ascending order
     * and thus using an iterator would give all the keys in a natural order
     * of keys, in our case testnames.
     * If the need be, we can provide a Comparator afterwards and change
     * the order of the keys.
     */
    TreeMap treeMap = null;

    Hashtable hash = null;

    /* CONSTRUCTORS */

    public statusU() {
        try {
            treeMap = new TreeMap();
            hash = new Hashtable();
            /*
            BufferedReader file = new BufferedReader(new FileReader("config.txt"));
            String line;

            while ((line = file.readLine()) != null) {
            StringTokenizer tokens = new StringTokenizer(line, ":");
            hash.put((String)tokens.nextElement(), (String)tokens.nextElement());
            }
            */

            hash.put("api_javaidl", "JavaIDL API Tests");
            hash.put("api_javax", "RMI-IIOP API Tests");
            hash.put("api_poa", "POA API Tests");
            hash.put("api_dynany", "DynAny API Tests");
            hash.put("api_ins", "INS API Tests");
            hash.put("api_orb", "ORB API Tests");
            hash.put("api_ior", "IOR API Tests");
            hash.put("api_giop", "GIOP API Tests");
            hash.put("api_ci", "Connection Interceptor API Tests");
            hash.put("api_InterfaceRepository",
                     "InterfaceRepository API Tests");
            hash.put("interoperability_evolution",
                     "Classes Evolution Interoperability Tests");
            hash.put("interoperability_strm2",
                     "Stream2 Evolution Interoperability Tests");
            hash.put("interoperability_serialization",
                     "Object Serialization Interoperability Tests");
            hash.put("interoperability_rmiiiop",
                     "RMI-IIOP Interoperability Tests");
            hash.put("performance_simpleperf",
                     "Local Optimization Performance Tests");
            hash.put("performance_interceptors",
                     "ClientInterceptor Performance Tests");
            hash.put("scalability_poa", "POA Scalability Tests");
            hash.put("scalability_activation",
                     "ORB Activation Deactivation Tests");
            hash.put("scalability_naming",
                     "NameService Scalability Tests");
            hash.put("endToend_pi", "EndToEnd PI Tests");
            hash.put("endToend_poa", "EndToEnd POA Tests");
            hash.put("Reliability", "Reliability Test");
            hash.put("product_rmijrmp", "RMI-JRMP Product Tests");
            hash.put("product_rmiiiop", "RMI-IIOP Product Tests");
            hash.put("product_pcosnaming", "PCosNaming Product Tests");
            hash.put("product_poa", "POA Product Tests");
            hash.put("product_pi", "PI Product Tests");
            hash.put("product_ots", "OTS Product Tests");
            hash.put("product_javaidl", "JavaIDL Product Tests");
            hash.put("product_valuetypes", "Valuetypes Product Tests");
            hash.put("product_saf", "Server Activation Tests");
            hash.put("product_orbspi", "PEORB SPI Tests");
            hash.put("product_ort", "ObjectReferenceTemplate Tests");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* METHODS */

    synchronized public void clearStatus() {
        treeMap.clear();
    }

    synchronized public void addStatus(String testName, int stat) {
        TestData testData = new TestData(stat, "No Description Available");

        treeMap.put(testName, testData);
    }

    synchronized public void addStatus(String testName, int stat, String desc) {
        if (desc == null) {
            desc = "No Description Available";
        }

        TestData testData = new TestData(stat, desc);

        treeMap.put(testName, testData);
    }

    synchronized public void addStatus(String testName, int status, String desc, int andOr) {
        if ((andOr != RTMConstants.AND) && (andOr != RTMConstants.OR)) {
            System.err.println("Invalid logical operator. Valid values are \"RTMConstants.AND\" or \"RTMConstants.OR\"");
            return;
        }

        if (desc == null) {
            desc = "No Description Available";
        }

        if (treeMap.containsKey(testName)) {
            TestData d = (TestData)treeMap.get(testName);
            TestData d2 = new TestData(logicalOp(d.getStatus(), status, andOr) == RTMConstants.PASS ? RTMConstants.PASS : RTMConstants.FAIL, d.getDesc() + "\n" + desc);
            treeMap.put(testName, d2);
        } else {
            addStatus(testName, status, desc);
        }
    }

    private int logicalOp(int status1, int status2, int op) {
        if (op == RTMConstants.AND) {
            if ((status1 == RTMConstants.PASS) && (status2 == RTMConstants.PASS))
                return RTMConstants.PASS;
            else
                return RTMConstants.FAIL;
        } else { // Default is OR operation. No need of error checking
            // here since input has already been validated
            if ((status1 == RTMConstants.FAIL) && (status2 == RTMConstants.FAIL))
                return RTMConstants.FAIL;
            else
                return RTMConstants.PASS;
        }
    }

    private String generateStatus() {
        String data = new String("\n");

        Set s = treeMap.keySet();
        Iterator i = s.iterator();

        while (i.hasNext()) {
            data += "<TEST-CASE>\n";

            String str = (String)i.next();
            TestData d = (TestData)treeMap.get(str);
            data += "<TEST-CASE-ID STATUS=\"" +
                ((d.getStatus() == RTMConstants.PASS) ? PASS : FAIL) +
                "\">" + str + "</TEST-CASE-ID>\n";

            if (!d.getDesc().equals(""))
                data += "<TEST-CASE-DESC>" + d.getDesc() + "</TEST-CASE-DESC>\n";

            data += "</TEST-CASE>\n\n";
        }

        return data;
    }

    synchronized private String generateHeader(String testName) {
        String data = new String("\n");

        String category;
        String suiteName;

        if (testName.startsWith("api"))
            category = "API";
        else if (testName.startsWith("product"))
            category = "PRODUCT";
        else if (testName.startsWith("endToend"))
            category = "END-TO-END";
        else if (testName.startsWith("Reliability"))
            category = "RELIABILITY";
        else if (testName.startsWith("interoperability"))
            category = "INTEROPERABILITY";
        else if (testName.startsWith("performance"))
            category = "PERFORMANCE";
        else if (testName.startsWith("scalability"))
            category = "SCALABILITY";
        else
            category = "No Description available";

        int index = testName.indexOf("_");
        if (index == -1) {
            index = testName.length();
        } else {
            index = testName.indexOf("_", index+1);
            if (index == -1) {
                index = testName.length();
            }
        }
        suiteName = (String)hash.get(testName.substring(0, index));

        data += "<TEST SUITE-NAME=\"" + suiteName + "\" CATEGORY=\"" + category + "\">\n";
        data += "<TEST-NAME>" + testName + "</TEST-NAME>\n";

        return data;
    }

    synchronized private String generateFooter(String testName) {
        String data = new String("\n");

        data += "<TEST-SUMMARY>\n";
        data += "<TEST-CASES-PASSED>" + this.totalPass() + "</TEST-CASES-PASSED>\n";
        data += "<TEST-CASES-FAILED>" + this.totalFail() + "</TEST-CASES-FAILED>\n";
        data += "</TEST-SUMMARY>\n";
        data += "</TEST>\n";

        return data;
    }

    synchronized public String generateSummary(String testName) {
        String data = generateHeader(testName);
        data += generateStatus();
        data += generateFooter(testName);

        return data;
    }

    synchronized public String generateSummary(String testName, String desc) {
        String data = generateHeader(testName);
        data += "<TEST-DESC>" + desc + "</TEST-DESC>\n";
        data += generateStatus();
        data += generateFooter(testName);

        return data;
    }

    synchronized public void printSummary(String testName) {
        System.out.println(generateSummary(testName));
    }

    synchronized public void printSummary(String testName, String desc) {
        System.out.println(generateSummary(testName, desc));
    }


    synchronized public int totalPass() {
        int pass = 0;

        Set s = treeMap.keySet();
        Iterator i = s.iterator();

        while (i.hasNext()) {
            String str = (String)i.next();
            TestData d = (TestData)treeMap.get(str);
            if (d.getStatus() == RTMConstants.PASS)
                pass++;
        }

        return pass;
    }


    synchronized public int totalFail() {
        int fail = 0;

        Set s = treeMap.keySet();
        Iterator i = s.iterator();

        while (i.hasNext()) {
            String str = (String)i.next();
            TestData d = (TestData)treeMap.get(str);
            if (d.getStatus() == RTMConstants.FAIL)
                fail++;
        }

        return fail;
    }

} // end class statusU



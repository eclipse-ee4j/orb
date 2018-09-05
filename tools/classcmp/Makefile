#
# Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

JAVA_HOME=$(ALT_BOOTDIR)
JAVA=$(JAVA_HOME)/bin/java
JAVAC=$(JAVA_HOME)/bin/javac
CLASSPATH="$(JAVA_HOME)/lib/tools.jar:."

build:
        $(JAVAC) -classpath $(CLASSPATH) ClassComparer.java
        $(JAVAC) -d tst1 tst1/TestClass.java
        $(JAVAC) -d tst2 tst2/TestClass.java

run:
        $(JAVA) -classpath $(CLASSPATH) ClassComparer tst1 tst2 TestClass


#!/usr/bin/python

# Copyright 2011 Red Hat Inc.
#
# This file is part of solenopsis
#
# solenopsis is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA


"""This class is designed as a wrapper for the ant methods.  Abstracts around the
various ant portions."""

__author__ = "Patrick Connelly (patrick@deadlypenguin.com)"
__version__ = "1.1"

import os
import sys

import logger
import environment

ANT_FLAGS = ""
BUILD_XML = "/usr/share/solenopsis/ant/solenopsis.xml"

ROOT_DIR = None

def setRootDir(path):
    """Sets the root dir for SFDC data

    path - The path
    """
    global ROOT_DIR
    ROOT_DIR = path

def getRootDir():
    """Gets the root dir for SFDC data"""
    return ROOT_DIR

def setBuildXML(path):
    """Sets the Build XML path

    path - The new build path
    """
    global BUILD_XML
    BUILD_XML=path

def getBuildXML():
    return BUILD_XML

def addFlag(flag):
    """Adds an ant flag to the current flag list

    flag - The flag to add
    """
    global ANT_FLAGS
    ANT_FLAGS += ' -D%s' % (flag,)

def getFlags():
    """Gets the ant flags"""
    return ANT_FLAGS

def runAnt(action):
    """Runs the ant action given

    action - The action to run
    """
    try:
        runString = 'ant %s -f %s %s' % (getFlags(), getBuildXML(), action,)
        logger.debug('Running ant command "%s"' % (runString,))
        retcode = os.system(runString)
        sys.exit(retcode >> 8)
    except OSError, (errno, strerror):
        print "Error running ant action '%'" % (strerror,)

def push():
    """Does a push to SFDC"""
    runAnt('push')

def gitPush():
    """Does a push of things that have changed in git"""
    runAnt('git-push')

def filePush(fileList):
    """Pushes individual files to SFDC

    fileList - An array of file names to push
    """
    if len(fileList) == 0:
        logger.critical('No files listed to push')
        sys.exit(-1)

    file_list = ''

    for fname in fileList:
        file_path = os.path.join(os.path.expanduser(getRootDir()), fname)
        if os.path.exists(file_path):
            file_list = "%s%s%s" %(file_list, fname, os.pathsep,)
        else:
            logger.warning('Unable to find file "%s".  Skipping.' % (file_path,))

    if not file_list == '':
        file_list[:-2]
        addFlag('%s=%s' % ('sf.files2push', file_list,))
        print getFlags()
        runAnt('file-push')
    else:
        logger.critical('Unable to find any files to push.')
        sys.exit(-1)

def destructivePush():
    """Does a destructive push to SFDC"""
    runAnt('destructive-push')

def gitDestructivePush():
    """Does a destructive push to SFDC of what has changed in git"""
    runAnt('git-destructive-push')

def pullFull():
    """Does a pull from SFDC to a temp dir"""
    runAnt('pull-full')

def pullFullToMaster():
    """Does a pull from SFDC to the root directory for that environment"""
    runAnt('pull-full-to-master')

def pull():
    """Does a pull from SFDC to a temp dir"""
    runAnt('pull')

def pullToMaster():
    """Does a pull from SFDC to the root directory for that environment"""
    runAnt('pull-to-master')

def describeMetadata():
    """Does a metadata describe from SFDC"""
    runAnt('describe-metadata')

def listMetadata(metadata_list):
    """Does a metadata list from SFDC"""
    addFlag('%s="%s"' % ('sf.metadataTypes', metadata_list,))
    runAnt('list-metadata')
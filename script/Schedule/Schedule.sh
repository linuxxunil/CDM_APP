#!/bin/sh
	
	hour=`date "+%H"`
	case $hour in
	00)hour=0;;
	01)hour=1;;
	02)hour=2;;
	03)hour=3;;
	04)hour=4;;
	05)hour=5;;
	06)hour=6;;
	07)hour=7;;
	08)hour=8;;
	09)hour=9;;
	esac
	PWD=/home/csmp/CSMP_CDM/Schedule
	java -jar $PWD/CDM_Utility.jar "http://mtm.vcap.me" $hour
	date >> /var/log/CDM_Schedule.log

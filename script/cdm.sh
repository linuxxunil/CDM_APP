#!/bin/bash
use_cloudfoundry=yes
ext_url="cdm.servehttp.com"
vblob_host="10.1.1.241"
vblob_port=9981
mysql_host="10.1.1.241"
mysql_port=3306
mysql_dbname="cbm"
log_host="127.0.0.1"
log_port=514
app_name="cdm"
memory=512
instance=1
push_app(){
echo -e "Y\n$app_name\nY\nY\n$memory\n$instance\nN\nN\n" | vmc push --no-start
vmc env-add $app_name "use_cloudfoundry=$use_cloudfoundry"
vmc env-add $app_name "vblob_host=$vblob_host"
vmc env-add $app_name "vblob_port=$vblob_port"
vmc env-add $app_name "mysql_host=$mysql_host"
vmc env-add $app_name "mysql_port=$mysql_port"
vmc env-add $app_name "dbname=$mysql_dbname"
vmc env-add $app_name "log_host=$log_host"
vmc env-add $app_name "log_port=$log_port"
vmc map $app_name $ext_url
vmc start $app_name	
}

del_app(){
vmc delete $app_name
}




case $1 in
start)
	push_app
;;
stop)
	del_app
;;
restart)
	del_app
	sleep 1
	push_app
;;
*)
	echo "$0 {start|stop|restart}"
;;
esac

#!/bin/bash

LOGFILE="filius.debug.log"

##
## extract information from filius.log
##
for nr in `grep 'DEBUG: InternetKnotenBetriebssystem' $LOGFILE | sort | uniq | sed 's/^.*(//' | sed 's/).*$//'`; do
  IKBstring=`grep -A 6 "DEBUG: InternetKnotenBetriebssystem ($nr)" $LOGFILE | \
    grep -v InternetKnotenBetriebssystem | \
    sed 's/^.*: //g' | \
    gawk '{ printf "%s ", $1 }'`
  hostID=`grep "has OS $nr" $LOGFILE | sed 's/ has OS.*$//g' | sed 's/^.* //g' | sed 's/ //g'`
  if [[ "x$hostID" == "x" ]]; then hostID="<kA>"; fi
  IKBstring="$IKBstring $hostID "
  ETHERNET=${IKBstring%% *}
  MAC_IP=`grep "($ETHERNET) .*(Ethernet), senden" $LOGFILE | head -n1 | sed 's/^.*quellMacAdresse=//g' | sed 's/, quellIp=/ /g' | sed 's/, .*$//g'`
  if [[ "x$MAC_IP" == "x" ]]; then MAC_IP="<kA> <kA>"; fi
  IKBstring="$IKBstring $MAC_IP "

  # remove double spaces (error-prone for gawk processing
  IKBstring=${IKBstring//  / }
  
  echo $IKBstring > IKB-$nr
done

##
## re-formatting of gathered information
##
> table.raw
for file in IKB*; do
  line="`head -n1 $file` ${file#*-}"
  echo $line | gawk '{ printf "%16s %18s %s %s %s %s %s %s %s %s", $9, $8, $1, $2, $3, $4, $5, $6, $7, $10 }' >> table.raw
  echo >> table.raw
done
sort table.raw > table.sorted

##
## write table of DEBUG information
##
> table.results
echo "==================================================================================================================================" >> table.results
echo "       IP        |        MAC        ||   KNOTEN   ||  ETHERNET  |     ARP    |     IP     |    ICMP    |     TCP    |     UDP    " >> table.results
echo "==================================================================================================================================" >> table.results
cat table.sorted | gawk '{ printf "%16s | %17s || %10s || %10s | %10s | %10s | %10s | %10s | %10s \n", $1, $2, $10, $3, $4, $5, $6, $7, $8, $9 }' >> table.results
echo "==================================================================================================================================" >> table.results
cat table.results

## 
## delete interim files
##
rm IKB-*
rm table.sorted table.raw

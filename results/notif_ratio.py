from parse import *
from datetime import datetime
import json
from sets import Set

def getDateTime(dateTimeString):
  dateObj = datetime.strptime(dateTimeString, "%y/%m/%d %H:%M:%S")
  return dateObj
  

with open('tez_logs.txt') as f:
  fullLogContent = f.readlines()
fullLogContent = [line.strip() for line in fullLogContent] 

with open('users.json') as usersFile:    
    usersJson = json.load(usersFile)
    
controlGroupIdSet = Set()
experimentGroupIdSet = Set()

for x in usersJson:
  if x['isControl']:
    controlGroupIdSet.add(x['deviceIdentifier'])
  else:
    experimentGroupIdSet.add(x['deviceIdentifier'])
    
print 'Control group id set-> ', controlGroupIdSet
print 'Experiment group id set ->',experimentGroupIdSet


controlNotificationSentDevices = Set() ### Active control devices
experimentNotificationSentDevices = Set() ### Active experiment devices

notifResultDict = {} ### Received reactions deviceID -> List['Positive' | 'Negative' | 'Dismissed']

controlNotifSentDict = {} ### Sent notifications notificationId -> time
controlReactionDict = {} ### Received reactions notificationId -> time

experimentNotifSentDict = {} ### Sent notifications notificationId -> time
experimentReactionDict = {} ### Received reactions notificationId -> time


### TO CALCULATE PASSED DAYS
firstLine = fullLogContent[0]
lastLine = fullLogContent[len(fullLogContent)-1]
parsedFirstLine = parse('{} INFO {}', firstLine)
parsedLastLine = parse('{} INFO {}', lastLine)
firstDate = getDateTime(parsedFirstLine[0])
lastDate = getDateTime(parsedLastLine[0])
totalTime = lastDate - firstDate
totalDays = totalTime.days + 1

### line examples:

#17/06/22 23:08:57 INFO RestApi: Received reaction results info: {"deviceIdentifier": "e826c00abd30f4c7", "reaction": "Dismissed", "notificationId": "91913764-f9b9-4c75-9881-6bc3690b3d7a"}   

#17/06/23 10:30:04 INFO FirebaseClient: 7b59c61295822abb Notification: {"data":{"notificationId":"74e53686-1f69-4cff-ae61-5678114e75b8"},"registration_ids":["edIlAI-pN-s:APA91bFe5JUiQXxcDZd_te8bqCFX5qsB7lk1Tt8fp4RMwuKyQtp65RUXE9BAqTStbzizwQQ0ApW655pkMPrF9JApp-nc10yt5IO6buGm6qx_C6U1pMXy_Vv5mosCyySvme0m6tREFJC2"]}
  
for line in fullLogContent:
  if "RestApi: Received reaction results info:" in line:
    parsed = parse('{} INFO RestApi: Received reaction results info: {}', line)
    dateString = parsed[0]
     
    data = json.loads(parsed[1])
    deviceId = data['deviceIdentifier']
    reaction = data['reaction']
    notifId = data['notificationId']
    
    ### If exists add to existing list, else create new list with one element
    notifResultDict[deviceId] = notifResultDict.get(deviceId, []) + [reaction]
    if deviceId in controlGroupIdSet:
      controlReactionDict[notifId] = getDateTime(dateString)
    elif deviceId in experimentGroupIdSet:
      experimentReactionDict[notifId] = getDateTime(dateString) 
  elif "FirebaseClient" in line and "registration_ids" in line:
    parsed = parse('{} INFO FirebaseClient: {} Notification: {}', line)
    dateString = parsed[0]
    deviceId = parsed[1]
    notificationJson = json.loads(parsed[2])
    if deviceId in controlGroupIdSet:
      controlNotificationSentDevices.add(deviceId)
      controlNotifSentDict[notificationJson['data']['notificationId']] = getDateTime(dateString)
    elif deviceId in experimentGroupIdSet:
      experimentNotificationSentDevices.add(deviceId)
      experimentNotifSentDict[notificationJson['data']['notificationId']] = getDateTime(dateString)
      
      
print "\nIntervention sent control users ", controlNotificationSentDevices
print "Intervention sent experiment users ", experimentNotificationSentDevices

### To calculate average reaction times
controlReactionTotalTime = 0
controlReactionTotalCount = 0

experimentReactionTotalTime = 0
experimentReactionTotalCount = 0


for notifId in controlNotifSentDict.keys():
  notifSentTime = controlNotifSentDict[notifId]
  reactionReceiveTime = controlReactionDict.get(notifId, -1)
  if reactionReceiveTime == -1:
    pass
    #print "No reaction for ", notifId
  else:
    timeDiff = reactionReceiveTime - notifSentTime
    controlReactionTotalTime += timeDiff.total_seconds()
    controlReactionTotalCount += 1
    
for notifId in experimentNotifSentDict.keys():
  notifSentTime = experimentNotifSentDict[notifId]
  reactionReceiveTime = experimentReactionDict.get(notifId, -1)
  if reactionReceiveTime == -1:
    pass
    #print "No reaction for ", notifId
  else:
    timeDiff = reactionReceiveTime - notifSentTime
    experimentReactionTotalTime += timeDiff.total_seconds()
    experimentReactionTotalCount += 1

### To calclate reaction type counts
controlGroupPositiveNotif = 0
controlGroupNegativeNotif = 0
controlGroupDismissedNotif = 0
experimentGroupPositiveNotif = 0
experimentGroupNegativeNotif = 0
experimentGroupDismissedNotif = 0

for a in notifResultDict.keys():
  if a in controlGroupIdSet:  
    for response in notifResultDict[a]:
      if response == 'Positive':
        controlGroupPositiveNotif += 1
      elif response == 'Negative':
        controlGroupNegativeNotif += 1
      elif response == 'Dismissed':
        controlGroupDismissedNotif += 1
  elif a in experimentGroupIdSet:
    for response in notifResultDict[a]:
      if response == 'Positive':
        experimentGroupPositiveNotif += 1
      elif response == 'Negative':
        experimentGroupNegativeNotif += 1
      elif response == 'Dismissed':
        experimentGroupDismissedNotif += 1
        

print "\nTotal days: ", totalDays

print "\nControl group average reaction time: ", controlReactionTotalTime / controlReactionTotalCount
print "Experiment group average reaction time: ", experimentReactionTotalTime / experimentReactionTotalCount
        
print '\nControl total number of interventions: ', len(controlNotifSentDict.keys())
print 'Experiment total number of interventions: ', len(experimentNotifSentDict.keys())

print '\nControl total number of reactions(Positive, Negative, Dismissed): ', controlGroupPositiveNotif, controlGroupNegativeNotif, controlGroupDismissedNotif
print 'Experiment total number of reactions(Positive, Negative, Dismissed): ', experimentGroupPositiveNotif, experimentGroupNegativeNotif, experimentGroupDismissedNotif

print '\nControl positive reaction/intervention ratio: %', float(controlGroupPositiveNotif)/ len(controlNotifSentDict.keys()) * 100
print 'Experiment positive reaction/intervention ratio: %', float(experimentGroupPositiveNotif)/ len(experimentNotifSentDict.keys()) * 100

print '\nControl daily intervention average per user(every): ', float(len(controlNotifSentDict.keys()))/ len(controlGroupIdSet) / totalDays
print 'Experiment daily intervention average per user(every): ', float(len(experimentNotifSentDict.keys()))/ len(experimentGroupIdSet) / totalDays

print '\nControl daily intervention average per user(active): ', float(len(controlNotifSentDict.keys()))/ len(controlNotificationSentDevices) / totalDays
print 'Experiment daily intervention average per user(active): ', float(len(experimentNotifSentDict.keys()))/ len(experimentNotificationSentDevices) / totalDays



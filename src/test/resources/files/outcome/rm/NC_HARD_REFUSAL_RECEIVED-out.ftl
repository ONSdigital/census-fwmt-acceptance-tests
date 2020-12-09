{
"event":{
"type":"REFUSAL_RECEIVED",
"source":"FIELDWORK_GATEWAY",
"channel":"FIELD",
"dateTime":"2020-04-17T12:53:11.000+01",
"transactionId":"b1646499-c5d8-4fbe-bb21-8e057601a3c2"
},
"payload":{
"refusal":{
"type":"${reason}",
"agentId":"SH-TWH1-ZA-25",
"isHouseholder": "${isHouseHolder?c}",
"contact": {
"title": "${encryptedTitle}",
"forename": "${encryptedForename}",
"surname": "${encryptedSurname}"
},
"collectionCase":{
"id":"bd6345af-d706-43d3-a13b-8c549e081a76"
}
}
}
}
{
"event":{
"type":"FULFILMENT_REQUESTED",
"source":"FIELDWORK_GATEWAY",
"channel":"FIELD",
"dateTime":"2020-04-17T12:53:11.000+01",
"transactionId":"b1646499-c5d8-4fbe-bb21-8e057601a3c2"
},
"payload":{
"fulfilmentRequest":{
"fulfilmentCode":"${fulfilmentCode}",
"caseId":"bd6345af-d706-43d3-a13b-8c549e081a76",
"address":{},
"contact": {
<#if requesterTitle??>
    "title": "${requesterTitle}",
<#else>
    "title":null,
</#if>
<#if requesterForename??>
    "forename":"${requesterForename}",
<#else>
    "forename":null,
</#if>
<#if requesterSurname??>
    "surname":"${requesterSurname}",
<#else>
    "surname":null,
</#if>
    "telNo":"07777000000"
}
<#if individualCaseId?? && surveyType == "HH">
    ,
    "individualCaseId": "${individualCaseId}"
<#else>
</#if>
<#--"title":null,-->
<#--"forename":null,-->
<#--"surname":null,-->
<#--"telNo":"07777000000"-->
}
}
}
}
}
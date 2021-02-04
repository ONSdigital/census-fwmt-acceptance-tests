{
"caseId":"bd6345af-d706-43d3-a13b-8c549e081a76",
"transactionId":"b1646499-c5d8-4fbe-bb21-8e057601a3c2",
"eventDate":"2020-04-17T11:53:11.000+0000",
"officerId":"SH-TWH1-ZA-25",
"coordinatorId":"SH-TWH1-ZA",
"primaryOutcomeDescription":"${primaryOutcomeDescription}",
"secondaryOutcomeDescription":"${secondaryOutcomeDescription}",
"outcomeCode":"${outcomeCode}",
"fulfilmentRequests":null,
<#if surveyType == "HH">
"refusal":{
"isHouseholder": "true",
"isDangerous": "true",
"contact": {
"title": "Mr",
"forename": "Joe",
"surname": "Bloggs"
}
<#else>
"refusal": null
</#if>
}
}
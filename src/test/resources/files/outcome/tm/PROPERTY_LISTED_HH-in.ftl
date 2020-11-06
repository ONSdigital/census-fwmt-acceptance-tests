{
"caseId":"bd6345af-d706-43d3-a13b-8c549e081a76",
"transactionId":"b1646499-c5d8-4fbe-bb21-8e057601a3c2",
"siteCaseId":"bd6345af-d706-43d3-a13b-8c549e081a76",
"eventDate":"2020-04-17T11:53:11.000+0000",
"officerId":"SH-TWH1-ZA-25",
"coordinatorId":"SH-TWH1-ZA",
"primaryOutcomeDescription":"${primaryOutcomeDescription}",
"secondaryOutcomeDescription":"${secondaryOutcomeDescription}",
"outcomeCode":"${outcomeCode}",
  "ceDetails": {
    "establishmentName": "Some home",
    "establishmentType": "Nursing home",
    "managerTitle": "Mr",
    "managerForename": "Frank",
    "managerSurname": "Spencer"
  },
 "address": {
    "addressLine1": "221b Baker St",
    "addressLine2": "Marylebone",
    "addressLine3": "string",
    "locality": "London",
    "postcode": "NW1 6XE",
    "latitude": 51.5237951,
    "longitude": -0.1582785
  },
"accessInfo":null,
"careCodes":["CAT","DOG"],
 <#if (linkedQid??) || (fulfilmentRequested??)>
    "fulfilmentRequests":[
    <#if linkedQid??>
       ${linkedQid}
        <#if fulfilmentRequested??>
         ,
        </#if>
    </#if>
    <#if fulfilmentRequested??>
       ${fulfilmentRequested}
    </#if>
    ]
<#else>
      "fulfilmentRequests":null
</#if>
}
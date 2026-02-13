const urlParams = new URLSearchParams(window.location.search);

const query = urlParams.get('query');
fetch(`/api/public/search/${query}`).then((result)=>{
    return result.json();
}).then((data)=>{
    const resultsDiv = document.getElementById("results-div");
    const noResultsFoundText = document.getElementById("no-res-found");

    const searchResultTemplate = document.getElementById("search-result-template");
    const searchResultText = document.getElementById("search-result-text")
    searchResultText.textContent = `Search results for '${query}'`;

    if (data.length>0) {
        for (let i=0;i<data.length;i++) {
            const searchResult = searchResultTemplate.cloneNode(true);
            searchResult.removeAttribute("id");
            searchResult.children[0].innerHTML = `<a href=/user/${data[i]['userId']}>${data[i]['username']}</a>`;
            searchResult.children[1].innerHTML = `<a href=https://www.worldcubeassociation.org/persons/${data[i]['wcaId']} target="_blank">${data[i]['wcaId']}</a>`;
            searchResult.children[2].textContent = data[i]['userId'];
            resultsDiv.appendChild(searchResult);
        }
    }else {
        noResultsFoundText.style.display="block";
    }
});
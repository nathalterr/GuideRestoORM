Dans les mappers, j'ai l'impression qu'il faut faire gaffe à la duplication de mêmes requêtes. Yen a deux qui partent sur les labels dans restaurantType, et j'ai l'impression que c'est faux. À vérifier

Complete Eval n'a pas d'ID ???

findRestaurantsByType dans user service en gros les labels je crois que c'est unique donc on pourrait sans autre faire qu'il prenne qu'un label bref

public void setDependenciesEval(CompleteEvaluationMapper completeEvalMapper,
GradeMapper gradeMapper,
BasicEvaluationMapper basicEvalMapper) {
}

    public void setDependenciesCityType(CityMapper cityMapper, RestaurantTypeMapper typeMapper){
        this.cityMapper = cityMapper;
        this.typeMapper = typeMapper;
    } a quoi sert cette merde


![img.png](img.png)


Chose à améliorer :
- Recherche par nom partiel
- Recherche par type : faire  un truc avec des chiffres comme pour les restos
- création resto, same qu'en dessus
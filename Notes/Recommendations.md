# Bayesian formulation for recommendations

We collect feedback for recommendation from the user in the form of likes/dislikes.

## Example data:

| CATEGORY/RESPONSE     | LIKED		| DISLIKED	| Total     |
| ------------------    | ------------  | -----------   | -------   |
| A			| 6		| 2		| 8         |
| B			| 2		| 2		| 4         |
| C			| 2		| 4		| 6         |
| Total		        | 10		| 8		| 18        |

## Bayes inference formula:

P(Liked|Category) = P(Category|Liked) * P(Liked) / (P(Category|Liked) * P(Liked) + P(Category|Disliked) * P(Disliked))

## Example calculation for the above data:

Priors (we can either choose these to adapt our model as we wish or choose them with the help of data we collect):
P(Liked) = 0.5
P(Disliked) = 0.5

-- Category A --
Likelihoods:
P(Category=A|Liked) = 0.6
P(Category=A|Disliked) = 0.25

Inference:
P(Liked|Category=A) = P(Category=A|Liked) * P(Liked) / (P(Category=A|Liked) * P(Liked) + P(Category=A|Disliked) * P(Disliked)) = 0.6 * 0.5 / (0.6 * 0.5 + 0.25 * 0.5) = 0.6 / (0.6 + 0.25) = 0.706

-- Category B --
Likelihoods:
P(Category=B|Liked) = 0.2
P(Category=B|Disliked) = 0.25

Inference:
P(Liked|Category=B) = P(Category=B|Liked) * P(Liked) / (P(Category=B|Liked) * P(Liked) + P(Category=B|Disliked) * P(Disliked)) = 0.2 / (0.2 + 0.25) = 0.4444

-- Category C --
Likelihoods:
P(Category=C|Liked) = 0.2
P(Category=C|Disliked) = 0.5

Inference:
P(Liked|Category=C) = P(Category=C|Liked) * P(Liked) / (P(Category=C|Liked) * P(Liked) + P(Category=C|Disliked) * P(Disliked)) = 0.2 / (0.2 + 0.5) = 0.286

These give us probability \_scores\_ for each category. In order to draw the weighted sample we compute probabilities for each category with the softmax function (so that they sum to 1 as:

P\_A = e^0.706/(e^0.706+e^0.444+e^0.286) = 0.412
P\_B = e^0.444/(e^0.706+e^0.444+e^0.286) = 0.317
P\_C = e^0.286/(e^0.706+e^0.444+e^0.286) = 0.271

Now, assume we have POIs p\_1, ..., p\_n and each p\_i belongs to a category C\_j. We write p\_i \in C\_j. Then the set of all POIs P = C\_1 \cup C\_2 \cup ... \cup C\_m. We want to choose k POIs from P weighted by their respective category's probability score. We can accomplish this as follows:

1. Draw a category, each is chosen with probability P\_i.
2. Draw a POI from the chosen category with uniform probability.
3. Repeat above steps k times.

/*
 * University of Central Florida
 * CAP4630 - Spring 2019
 * Authors: <John Mirschel, Wen Lam>
 */

%%%%%%%%% Simple Prolog Planner %%%%%%%%%%%%%%%%%%%%%%%%%%
%%%
%%% Based on one of the sample programs in:
%%%
%%% Artificial Intelligence:
%%% Structures and strategies for complex problem solving
%%%
%%% by George F. Luger and William A. Stubblefield
%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

:- module( planner,[
	       plan/4,
	       change_state/3,
	       conditions_met/2,
	       member_state/2,
	       move/3,
	       go/2,
	       test1/0,
	       test2/0
	   ]).

:- [utils].

plan(State, Goal, _, Moves) :-
				equal_set(State, Goal),
				write('moves are'), nl,
				reverse_print_stack(Moves).

plan(State, Goal, Been_list, Moves) :-
				move(Name, Preconditions, Actions),
				conditions_met(Preconditions, State),
				change_state(State, Actions, Child_state),
				not(member_state(Child_state, Been_list)),
				stack(Child_state, Been_list, New_been_list),
				stack(Name, Moves, New_moves),
				plan(Child_state, Goal, New_been_list, New_moves),!.

change_state(S, [], S).

change_state(S, [add(P)|T], S_new) :-
						change_state(S, T, S2),
						add_to_set(P, S2, S_new), !.

change_state(S, [del(P)|T], S_new) :-
						change_state(S, T, S2),
						remove_from_set(P, S2, S_new), !.

conditions_met(P, S) :- subset(P, S).

member_state(S, [H|_]) :- equal_set(S, H).

member_state(S, [_|T]) :- member_state(S, T).

/* move types (inclusion of room(x, z) where x is in room z) */
move(pickup(X),
		[handempty, clear(X), on(X, Y), room(X, Z), room(Y, Z), room(arm, Z)],
		[del(handempty), del(clear(X)), del(on(X, Y)), del(room(X, Z)), add(clear(Y)), add(holding(X))]).

move(pickup(X),
		[handempty, clear(X), ontable(X), room(X, Z), room(arm, Z)],
		[del(handempty), del(clear(X)), del(ontable(X)), del(room(X, Z)), add(holding(X))]).

move(putdown(X),
		[holding(X), room(arm, Z)],
		[del(holding(X)), add(ontable(X)), add(clear(X)), add(handempty), add(room(X, Z))]).

move(stack(X, Y),
		[holding(X), clear(Y), room(Y, Z), room(arm, Z)],
		[del(holding(X)), del(clear(Y)), add(handempty), add(on(X, Y)), add(clear(X)), add(room(X, Z))]).

/* added predicates to determine movements of the arm to different rooms */
move(goroom1,
		[room(arm, 2)],
		[del(room(arm, 2)), add(room(arm, 1))]).

move(goroom2,
		[room(arm, 1)],
		[del(room(arm, 1)), add(room(arm, 2))]).

/* run commands */
go(S, G) :- plan(S, G, [S], []).

/* original test cases for 1 room planner */
/*
test :- go([handempty, ontable(b), ontable(c), on(a, b), clear(c), clear(a)],
				[handempty, ontable(c), on(a,b), on(b, c), clear(a)]).

test2 :- go([handempty, ontable(b), ontable(c), on(a, b), clear(c), clear(a)],
				[handempty, ontable(a), ontable(b), on(c, b), clear(a), clear(c)]).
*/

/* new test cases for 2 room planner (inclusion of room(x, z)) */
test1 :- go([handempty, ontable(b), ontable(c), on(a, b), clear(c), clear(a), room(a, 1), room(b, 1), room(c, 1), room(arm, 1)],
				[handempty, ontable(c), on(b, c), on(a, b), clear(a), room(a, 1), room(b, 1), room(c, 1), room(arm, 1)]).

test2 :- go([handempty, ontable(b), ontable(c), on(a, b), clear(c), clear(a), room(a, 1), room(b, 1), room(c, 1), room(arm, 1)],
				[handempty, ontable(b), on(c, b), on(a, c), clear(a), room(a, 2), room(b, 2), room(c, 2), room(arm, 1)]).